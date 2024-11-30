package com.decmoe47.todo.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.decmoe47.todo.constant.SessionAttributeKeys;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.service.VerificationCodeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final HttpSession session;

    public String createCode() {
        String code = RandomUtil.randomNumbers(4);
        session.setAttribute(SessionAttributeKeys.VERIFICATION_CODE, code);
        session.setAttribute(SessionAttributeKeys.VERIFICATION_CODE_EXPIRE_TIME, LocalDateTime.now().plusMinutes(5));
        return code;
    }

    public String getCode() {
        String code = (String) session.getAttribute(SessionAttributeKeys.VERIFICATION_CODE);
        LocalDateTime expireTime = (LocalDateTime) session.getAttribute(SessionAttributeKeys.VERIFICATION_CODE_EXPIRE_TIME);
        if (expireTime.isAfter(LocalDateTime.now())) {
            session.removeAttribute(SessionAttributeKeys.VERIFICATION_CODE);
            throw new ErrorResponseException(ErrorCodeEnum.VERIFY_CODE_EXPIRED);
        }
        return code;
    }

    public void assertSameCode(String source, @Nullable String target) {
        if (!source.equals(target))
            throw new ErrorResponseException(ErrorCodeEnum.VERIFY_CODE_INCORRECT);
    }

    public void checkCode(@Nullable String target) {
        assertSameCode(getCode(), target);
    }
}