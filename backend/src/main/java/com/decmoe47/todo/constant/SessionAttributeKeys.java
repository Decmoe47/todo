package com.decmoe47.todo.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionAttributeKeys {

    public static final String VERIFICATION_CODE = "verificationCode";
    public static final String VERIFICATION_CODE_EXPIRE_TIME = "verificationCodeExpireTime";
}
