package com.decmoe47.todo.model.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SendVerifyCodeRequest(
    @field:NotBlank
    @field:Email
    val email: String
)
