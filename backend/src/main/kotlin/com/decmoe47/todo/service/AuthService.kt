package com.decmoe47.todo.service

import com.decmoe47.todo.model.request.UserLoginRequest
import com.decmoe47.todo.model.request.UserRegisterRequest
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import com.decmoe47.todo.model.response.UserResponse

interface AuthService {
    fun login(request: UserLoginRequest): UserResponse

    fun logout(token: String)

    fun register(request: UserRegisterRequest): UserResponse

    fun sendVerificationCode(email: String)

    fun refreshAccessToken(refreshToken: String): AuthenticationTokensResponse
}