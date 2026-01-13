package com.decmoe47.todo.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.decmoe47.todo.config.property.SecurityProperties
import com.decmoe47.todo.constant.JwtConstants
import com.decmoe47.todo.constant.RedisConstants
import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.dto.SecurityUser
import com.decmoe47.todo.service.impl.TokenServiceImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.mockk.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.util.*
import java.util.concurrent.TimeUnit

class TokenServiceImplTest : FunSpec({
    val securityProperties = SecurityProperties(
        accessTokenTtl = 60,
        refreshTokenTtl = 120,
        secretKey = "secret"
    )

    lateinit var redisTemplate: RedisTemplate<String, Any?>
    lateinit var valueOps: ValueOperations<String, Any?>
    lateinit var service: TokenServiceImpl

    beforeTest {
        redisTemplate = mockk()
        valueOps = mockk(relaxed = true)
        every { redisTemplate.opsForValue() } returns valueOps

        service = TokenServiceImpl(securityProperties, redisTemplate)
    }

    test("generate returns tokens for security user") {
        val user = SecurityUser(id = 7, email = "user@test.com")
        val authentication = UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())

        val tokens = service.generate(authentication)

        tokens.accessToken.shouldNotBeBlank()
        tokens.refreshToken.shouldNotBeBlank()
    }

    test("generate throws when principal is not security user") {
        val authentication = UsernamePasswordAuthenticationToken("user", null, emptyList())

        val error = shouldThrow<ErrorResponseException> {
            service.generate(authentication)
        }

        error.errorCode shouldBe ErrorCode.USER_NOT_FOUND
    }

    test("parse extracts security user from token") {
        val user = SecurityUser(id = 10, email = "user@test.com")
        val authentication = UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        val tokens = service.generate(authentication)

        val parsed = service.parse("${JwtConstants.BEARER_PREFIX}${tokens.accessToken}")

        val principal = parsed.principal as SecurityUser
        principal.id shouldBe 10
        principal.email shouldBe "user@test.com"
    }

    test("isValid returns true when token is signed and not blacklisted") {
        val tokenId = "token-id"
        val token = JWT.create()
            .withJWTId(tokenId)
            .sign(Algorithm.HMAC256(securityProperties.secretKey))

        every { redisTemplate.hasKey(RedisConstants.BLACKLIST_TOKEN.format(tokenId)) } returns false

        service.isValid("${JwtConstants.BEARER_PREFIX}$token") shouldBe true
    }

    test("isValid returns false when token is blacklisted") {
        val tokenId = "token-id"
        val token = JWT.create()
            .withJWTId(tokenId)
            .sign(Algorithm.HMAC256(securityProperties.secretKey))

        every { redisTemplate.hasKey(RedisConstants.BLACKLIST_TOKEN.format(tokenId)) } returns true

        service.isValid(token) shouldBe false
    }

    test("isValid returns false for malformed token") {
        service.isValid("bad.token.value") shouldBe false
    }

    test("refresh returns new access token and keeps refresh token") {
        val user = SecurityUser(id = 3, email = "user@test.com")
        val authentication = UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        val tokens = service.generate(authentication)

        val refreshed = service.refresh(tokens.refreshToken)

        refreshed.refreshToken shouldBe tokens.refreshToken
        refreshed.accessToken.shouldNotBeBlank()
    }

    test("refresh throws when token missing claims") {
        val token = JWT.create()
            .withJWTId("id")
            .sign(Algorithm.HMAC256(securityProperties.secretKey))

        val error = shouldThrow<ErrorResponseException> {
            service.refresh(token)
        }

        error.errorCode shouldBe ErrorCode.REFRESH_TOKEN_EXPIRED
    }

    test("invalidate throws when token has no id") {
        val token = JWT.create()
            .sign(Algorithm.HMAC256(securityProperties.secretKey))

        val error = shouldThrow<ErrorResponseException> {
            service.invalidate(token)
        }

        error.errorCode shouldBe ErrorCode.ACCESS_TOKEN_EXPIRED
    }

    test("invalidate skips blacklisting when token already expired") {
        val token = JWT.create()
            .withJWTId("expired")
            .withExpiresAt(Date(0))
            .sign(Algorithm.HMAC256(securityProperties.secretKey))

        every { valueOps.set(any(), any(), any(), any<TimeUnit>()) } just runs
        every { valueOps.set(any(), any()) } just runs

        service.invalidate("${JwtConstants.BEARER_PREFIX}$token")

        verify(exactly = 0) { valueOps.set(any(), any(), any(), any<TimeUnit>()) }
        verify(exactly = 0) { valueOps.set(any(), any()) }
    }

    test("invalidate blacklists non-expiring token") {
        val tokenId = "non-expiring"
        val token = JWT.create()
            .withJWTId(tokenId)
            .sign(Algorithm.HMAC256(securityProperties.secretKey))

        every { valueOps.set(any(), any()) } just runs

        service.invalidate(token)

        verify { valueOps.set(RedisConstants.BLACKLIST_TOKEN.format(tokenId), "") }
    }

    test("invalidate blacklists active token with ttl") {
        val tokenId = "active"
        val token = JWT.create()
            .withJWTId(tokenId)
            .withExpiresAt(Date(System.currentTimeMillis() + 60_000))
            .sign(Algorithm.HMAC256(securityProperties.secretKey))

        every { valueOps.set(any(), any(), any(), any<TimeUnit>()) } just runs

        service.invalidate("${JwtConstants.BEARER_PREFIX}$token")

        verify {
            valueOps.set(
                RedisConstants.BLACKLIST_TOKEN.format(tokenId),
                "",
                any<Long>(),
                TimeUnit.SECONDS
            )
        }
    }
})
