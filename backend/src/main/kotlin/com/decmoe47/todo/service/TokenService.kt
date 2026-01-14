package com.decmoe47.todo.service

import com.decmoe47.todo.model.dto.SecurityUser
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import org.springframework.security.core.Authentication

interface TokenService {
    fun generate(authentication: Authentication): AuthenticationTokensResponse

    fun generate(user: SecurityUser): AuthenticationTokensResponse

    fun parse(token: String): Authentication

    fun isValid(token: String): Boolean

    fun refresh(refreshToken: String): AuthenticationTokensResponse

    fun invalidate(token: String)
}
