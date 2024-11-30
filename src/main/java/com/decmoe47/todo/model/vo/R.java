package com.decmoe47.todo.model.vo;

import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@Accessors(chain = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Value
public class R<T> implements Serializable {

    int code;
    String message;

    @Nullable
    T data;

    public static <T> R<T> ok() {
        return new R<>(0, "", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(0, "", data);
    }

    public static <T> R<T> ok(ErrorCodeEnum errCode) {
        return new R<>(errCode.getCode(), errCode.getMessage(), null);
    }

    public static <T> R<T> ok(ErrorCodeEnum errCode, T data) {
        return new R<>(errCode.getCode(), errCode.getMessage(), data);
    }
}
