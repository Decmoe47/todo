package com.decmoe47.todo.constant;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityConstants {

    /**
     * 无需授权就可访问的router
     */
    public static final List<String> AUTH_WHITELIST = List.of("/api/auth/login", "/api/auth/register",
            "/api/auth/logout", "/api/auth/send-verify-code", "/api/auth/refresh-token");
}
