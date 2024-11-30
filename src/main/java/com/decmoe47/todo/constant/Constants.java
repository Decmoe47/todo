package com.decmoe47.todo.constant;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    /**
     * Base64密钥
     */
    public static final String BASE64_SECRET = "ZmQ0ZGI5NjQ0MDQwY2I4MjMxY2Y3ZmI3MjdhN2ZmMjNhODViOTg1ZGE0NTBjMGM4NDA5NzYxMjdjOWMwYWRmZTBlZjlhNGY3ZTg4Y2U3YTE1ODVkZDU5Y2Y3OGYwZWE1NzUzNWQ2YjFjZDc0NGMxZWU2MmQ3MjY1NzJmNTE0MzI=";

    /**
     * 无需授权就可访问的router
     */
    public static final String[] AUTH_WHITELIST = {
            "/api/user/login", "/api/user/logout", "/api/user/register"
    };

    /**
     * 用户在redis中的缓存时长（日）
     */
    public static final int USER_CACHE_DAYS = 7;


}
