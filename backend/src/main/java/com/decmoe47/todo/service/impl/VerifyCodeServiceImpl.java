package com.decmoe47.todo.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.decmoe47.todo.constant.SessionAttributeKeys;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.service.VerifyCodeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {

    private final HttpSession session;

    public String createCode() {
        String code = RandomUtil.randomNumbers(4);
        session.setAttribute(SessionAttributeKeys.VERIFY_CODE, code);
        session.setAttribute(SessionAttributeKeys.VERIFY_CODE_EXPIRE_TIME, LocalDateTime.now().plusMinutes(5));
        return code;
    }

    public String getCode() {
        String code = (String) session.getAttribute(SessionAttributeKeys.VERIFY_CODE);
        LocalDateTime expireTime = (LocalDateTime) session.getAttribute(SessionAttributeKeys.VERIFY_CODE_EXPIRE_TIME);
        if (LocalDateTime.now().isAfter(expireTime)) {
            session.removeAttribute(SessionAttributeKeys.VERIFY_CODE);
            throw new ErrorResponseException(ErrorCodeEnum.VERIFY_CODE_EXPIRED);
        }
        return code;
    }

    public void assertSameCode(@Nullable String source, @Nullable String target) {
        if (!StrUtil.equals(source, target))
            throw new ErrorResponseException(ErrorCodeEnum.VERIFY_CODE_INCORRECT);
    }

    public void checkCode(@Nullable String target) {
        assertSameCode(getCode(), target);
    }
}