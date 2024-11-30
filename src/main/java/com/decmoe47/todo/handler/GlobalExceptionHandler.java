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
    public R<?> handleErrorResponseException(ErrorResponseException e) {
        if (e.getData() != null) {
            return R.ok(e.getErrorCode(), e.getData());
        } else {
            return R.ok(e.getErrorCode());
        }
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("{}\n{}", e.getMessage(), ExceptionUtils.getStackTrace(e));  // 记录错误信息
        return R.ok(ErrorCodeEnum.INTERNAL_SERVER_ERROR);
    }
}