package com.decmoe47.todo.model.request

import jakarta.validation.constraints.Min

data class UserSearchRequest(
    @field:Min(1)
    val id: Long?,
    val name: String?,
    val email: String?,
)
