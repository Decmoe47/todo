package com.decmoe47.todo.service

import com.decmoe47.todo.constant.RedisConstants
import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Service
class VerificationCodeServiceImpl(private val redisTemplate: RedisTemplate<String, Any>) : VerificationCodeService {
    override fun createCode(email: String): String {
        val code: String = Random.nextInt(1001, 9999).toString()
        redisTemplate.opsForValue().set(
            RedisConstants.VERIFICATION_CODE.format(email), code, //NOSONAR
            5, TimeUnit.MINUTES
        )
        return code
    }

    override fun getCode(email: String): String =
        redisTemplate.opsForValue()[RedisConstants.VERIFICATION_CODE.format(email)] as? String
            ?: throw ErrorResponseException(ErrorCode.VERIFICATION_CODE_EXPIRED)

    override fun assertSameCode(source: String, target: String) {
        if (source != target) throw ErrorResponseException(ErrorCode.VERIFICATION_CODE_INCORRECT)
    }

    override fun checkCode(target: String, email: String) = assertSameCode(getCode(email), target)
}