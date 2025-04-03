package com.decmoe47.todo.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionAttributeKeys {

    public static final String VERIFY_CODE = "verifyCode";
    public static final String VERIFY_CODE_EXPIRE_TIME = "verifyCodeExpireTime";
}
