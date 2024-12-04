package com.decmoe47.todo.model.vo;

import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class R<T> implements Serializable {

    private int code;
    private String message;

    @Nullable
    private T data;

    public static <T> R<T> ok() {
        return new R<>(0, "", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(0, "", data);
    }

    public static <T> R<T> error(ErrorCodeEnum errCode) {
        return new R<>(errCode.getCode(), errCode.getMessage(), null);
    }

    public static <T> R<T> error(ErrorCodeEnum errCode, T data) {
        return new R<>(errCode.getCode(), errCode.getMessage(), data);
    }
}
