package com.decmoe47.todo.handler;

import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.model.vo.R;
import com.decmoe47.todo.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.decmoe47.todo.controller")
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(Throwable.class)
    public R<Object> handleException(HttpServletRequest request, Throwable e) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause == null) {
            rootCause = e;
        }

        switch (rootCause) {
            case ErrorResponseException errorResponseException -> {
                return handleErrorResponseException(errorResponseException);
            }
            case AuthorizationDeniedException authorizationDeniedException -> {
                return handleAuthorizationDeniedException(request, authorizationDeniedException);
            }
            default -> {
                log.error("{}\n{}", rootCause.getMessage(), ExceptionUtils.getStackTrace(rootCause));
                return R.error(ErrorCodeEnum.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @ResponseBody
    @ExceptionHandler(ErrorResponseException.class)
    public R<Object> handleErrorResponseException(ErrorResponseException e) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause == null) {
            rootCause = e;
        }
        log.error("{}\n{}", rootCause.getMessage(), ExceptionUtils.getStackTrace(rootCause));
        if (e.getData() != null) {
            return R.error(e.getErrorCode(), e.getData());
        } else {
            return R.error(e.getErrorCode());
        }
    }

    @ResponseBody
    @ExceptionHandler(AuthorizationDeniedException.class)
    public R<Object> handleAuthorizationDeniedException(HttpServletRequest request, AuthorizationDeniedException e) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause == null) {
            rootCause = e;
        }
        log.error("{} for userId: {} and api: {} \n{}", rootCause.getMessage(), SecurityUtil.getCurrentUserId(),
                request.getRequestURI(), ExceptionUtils.getStackTrace(rootCause));
        return R.error(ErrorCodeEnum.ACCESS_DENIED);
    }
}