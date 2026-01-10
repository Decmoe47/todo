package com.decmoe47.todo.service

import com.decmoe47.todo.constant.RedisConstants
import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.service.impl.VerificationCodeServiceImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit

class VerificationCodeServiceImplTest : FunSpec({
    lateinit var redisTemplate: RedisTemplate<String, Any>
    lateinit var valueOps: ValueOperations<String, Any>
    lateinit var service: VerificationCodeServiceImpl

    beforeTest {
        redisTemplate = mockk()
        valueOps = mockk(relaxed = true)
        every { redisTemplate.opsForValue() } returns valueOps

        service = VerificationCodeServiceImpl(redisTemplate)
    }

    test("createCode stores code in redis and returns it") {
        val codeSlot = slot<String>()
        every { valueOps.set(any(), capture(codeSlot), any(), any<TimeUnit>()) } returns Unit

        val code = service.createCode("user@test.com")

        code shouldBe codeSlot.captured
        verify {
            valueOps.set(
                RedisConstants.VERIFICATION_CODE.format("user@test.com"),
                any(),
                5,
                TimeUnit.MINUTES
            )
        }
    }

    test("getCode returns stored code") {
        every { valueOps[RedisConstants.VERIFICATION_CODE.format("user@test.com")] } returns "1234"

        service.getCode("user@test.com") shouldBe "1234"
    }

    test("getCode throws when code is missing") {
        every { valueOps[RedisConstants.VERIFICATION_CODE.format("user@test.com")] } returns null

        val error = shouldThrow<ErrorResponseException> {
            service.getCode("user@test.com")
        }

        error.errorCode shouldBe ErrorCode.VERIFICATION_CODE_EXPIRED
    }

    test("checkCode throws when code does not match") {
        every { valueOps[RedisConstants.VERIFICATION_CODE.format("user@test.com")] } returns "1111"

        val error = shouldThrow<ErrorResponseException> {
            service.checkCode("2222", "user@test.com")
        }

        error.errorCode shouldBe ErrorCode.VERIFICATION_CODE_INCORRECT
    }

    test("checkCode returns when code matches") {
        every { valueOps[RedisConstants.VERIFICATION_CODE.format("user@test.com")] } returns "1234"

        service.checkCode("1234", "user@test.com")
    }
})
