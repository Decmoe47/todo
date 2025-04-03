package com.decmoe47.todo.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.decmoe47.todo.constant.RedisConstants;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.service.VerificationCodeService;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public String createCode(String email) {
        String code = RandomUtil.randomNumbers(4);
        redisTemplate.opsForValue().set(StrUtil.format(RedisConstants.VERIFICATION_CODE, email), code,
                5, TimeUnit.MINUTES);
        return code;
    }

    public String getCode(String email) {
        Object code = redisTemplate.opsForValue().get(StrUtil.format(RedisConstants.VERIFICATION_CODE, email));
        if (code == null)
            throw new ErrorResponseException(ErrorCodeEnum.VERIFICATION_CODE_EXPIRED);
        return (String) code;
    }

    public void assertSameCode(@Nullable String source, @Nullable String target) {
        if (!StrUtil.equals(source, target))
            throw new ErrorResponseException(ErrorCodeEnum.VERIFICATION_CODE_INCORRECT);
    }

    public void checkCode(@Nullable String target, String email) {
        assertSameCode(getCode(email), target);
    }
}