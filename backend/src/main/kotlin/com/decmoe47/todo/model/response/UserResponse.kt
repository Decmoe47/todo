package com.decmoe47.todo.model.response

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val token: AuthenticationTokensResponse? = null,
)
