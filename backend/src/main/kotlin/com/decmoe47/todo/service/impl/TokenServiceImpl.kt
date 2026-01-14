package com.decmoe47.todo.service.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.decmoe47.todo.config.property.SecurityProperties
import com.decmoe47.todo.constant.JwtConstants
import com.decmoe47.todo.constant.RedisConstants
import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.dto.SecurityUser
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import com.decmoe47.todo.service.TokenService
import jakarta.annotation.Resource
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class TokenServiceImpl(
    private val securityProperties: SecurityProperties, @Resource private val redisTemplate: RedisTemplate<String, Any?>
) : TokenService {

    private val algorithm by lazy { Algorithm.HMAC256(securityProperties.secretKey) }

    override fun generate(authentication: Authentication): AuthenticationTokensResponse {
        val user = authentication.principal as? SecurityUser ?: throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)
        return generate(user)
    }

    override fun generate(user: SecurityUser): AuthenticationTokensResponse {
        return AuthenticationTokensResponse(
            accessToken = generateToken(user, securityProperties.accessTokenTtl),
            refreshToken = generateToken(user, securityProperties.refreshTokenTtl)
        )
    }

    override fun parse(token: String): Authentication =
        getUserFromToken(token).let { user ->
            UsernamePasswordAuthenticationToken(user, token, user.getAuthorities())
        }

    override fun isValid(token: String): Boolean {
        try {
            val verifier = JWT.require(algorithm).build()
            val jwt = verifier.verify(token.stripBearer())
            return !redisTemplate.hasKey(
                RedisConstants.BLACKLIST_TOKEN.format(jwt.id),
            )
        } catch (_: JWTVerificationException) {
            return false
        }
    }

    override fun refresh(refreshToken: String): AuthenticationTokensResponse =
        getUserFromToken(refreshToken, isRefreshToken = true).let { user ->
            AuthenticationTokensResponse(generateToken(user, securityProperties.accessTokenTtl), refreshToken)
        }

    override fun invalidate(token: String) {
        val jwt = JWT.decode(token.stripBearer())
        val jwtId: String = jwt.id ?: throw ErrorResponseException(ErrorCode.ACCESS_TOKEN_EXPIRED)
        val expiresAt: Long? = jwt.expiresAt?.time

        val blacklistTokenKey = RedisConstants.BLACKLIST_TOKEN.format(jwtId)

        if (expiresAt != null) {
            val currentTimeSeconds = System.currentTimeMillis()
            if (expiresAt < currentTimeSeconds) {
                // Token已过期，直接返回
                return
            }
            // 计算Token剩余时间，将其加入黑名单
            val expirationIn = expiresAt - currentTimeSeconds
            redisTemplate.opsForValue().set(blacklistTokenKey, "", expirationIn, TimeUnit.SECONDS) //NOSONAR
        } else {
            // 永不过期的Token永久加入黑名单
            redisTemplate.opsForValue()[blacklistTokenKey] = ""
        }
    }

    private fun generateToken(user: SecurityUser, ttl: Int): String {
        val jwt = JWT.create()
            .withIssuedAt(Date())
            .withSubject(user.email)
            .withClaim(JwtConstants.USER_ID, user.id)
            .withClaim(JwtConstants.EMAIL, user.email)
            .withJWTId(UUID.randomUUID().toString())

        // 设置过期时间 -1 表示永不过期
        if (ttl != -1) {
            val expiresAt = System.currentTimeMillis() + ttl * 1000L
            jwt.withExpiresAt(Date(expiresAt))
        }

        return jwt.sign(algorithm)
    }

    private fun getUserFromToken(token: String, isRefreshToken: Boolean = false): SecurityUser {
        val jwt = JWT.decode(token.stripBearer())
        val cause = if (isRefreshToken) ErrorCode.REFRESH_TOKEN_EXPIRED else ErrorCode.ACCESS_TOKEN_EXPIRED
        val userId = jwt.getClaim(JwtConstants.USER_ID).asLong() ?: throw ErrorResponseException(cause)
        val email = jwt.getClaim(JwtConstants.EMAIL).asString() ?: throw ErrorResponseException(cause)

        return SecurityUser(id = userId, email = email)
    }

    private fun String.stripBearer() = removePrefix(JwtConstants.BEARER_PREFIX)
}