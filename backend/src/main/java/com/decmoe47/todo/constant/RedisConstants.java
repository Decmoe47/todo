package com.decmoe47.todo.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisConstants {

    public static final String BLACKLIST_TOKEN = "auth:token:blacklist:{}";
}
