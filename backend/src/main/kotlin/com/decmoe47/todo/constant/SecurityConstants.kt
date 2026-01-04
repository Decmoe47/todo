package com.decmoe47.todo.constant

object SecurityConstants {
    val AUTH_WHITELIST: List<String> = listOf(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/logout",
        "/api/auth/send-verify-code",
        "/api/auth/refresh-token"
    )
}