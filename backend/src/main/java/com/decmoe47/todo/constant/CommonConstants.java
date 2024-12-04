package com.decmoe47.todo.constant;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonConstants {

    /**
     * 无需授权就可访问的router
     */
    public static final String[] AUTH_WHITELIST = {
            "/api/auth/**"
    };
}
