package com.decmoe47.todo.handler;

import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.model.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.decmoe47.todo.controller")
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(ErrorResponseException.class)
    public R<Object> handleErrorResponseException(ErrorResponseException e) {
        log.error("{}\n{}", e.getMessage(), ExceptionUtils.getStackTrace(e));
        if (e.getData() != null) {
            return R.error(e.getErrorCode(), e.getData());
        } else {
            return R.error(e.getErrorCode());
        }
    }

    @ResponseBody
    @ExceptionHandler(Throwable.class)
    public R<Object> handleException(Throwable e) {
        log.error("{}\n{}", e.getMessage(), ExceptionUtils.getStackTrace(e));
        return R.error(ErrorCodeEnum.INTERNAL_SERVER_ERROR);
    }
}