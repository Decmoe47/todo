package com.decmoe47.todo.exception;

import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode(callSuper = true)
@Value
public class ErrorResponseException extends RuntimeException {

    ErrorCodeEnum errorCode;

    @Nullable
    Object data;

    public ErrorResponseException(ErrorCodeEnum errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = null;
    }

    public ErrorResponseException(ErrorCodeEnum errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.data = null;
    }

    public ErrorResponseException(ErrorCodeEnum errorCode, Object data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = data;
    }

    public ErrorResponseException(ErrorCodeEnum errorCode, Object data, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.data = data;
    }

    public ErrorResponseException(ErrorCodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.data = null;
    }

    public ErrorResponseException(ErrorCodeEnum errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.data = null;
    }

    public ErrorResponseException(ErrorCodeEnum errorCode, String message, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }

    public ErrorResponseException(ErrorCodeEnum errorCode, String message, Object data, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.data = data;
    }
}
