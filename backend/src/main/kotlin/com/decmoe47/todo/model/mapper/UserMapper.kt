package com.decmoe47.todo.model.mapper

import com.decmoe47.todo.model.entity.User
import com.decmoe47.todo.model.request.UserRegisterRequest
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import com.decmoe47.todo.model.response.UserResponse

fun User.toUserResponse(token: AuthenticationTokensResponse? = null): UserResponse {
    return UserResponse(
        id = this.id,
        email = this.email,
        name = this.name,
        token = token
    )
}

fun UserRegisterRequest.toUser(): User {
    return User(
        email = this.email,
        name = this.name,
        password = this.password
    )
}