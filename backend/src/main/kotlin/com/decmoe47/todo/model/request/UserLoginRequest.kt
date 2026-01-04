package com.decmoe47.todo.model.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserLoginRequest(
    @field:Email
    @field:NotBlank(message = "邮箱不能为空！")
    val email: String,

    @field:NotBlank(message = "密码不能为空！")
    val password: String,
)
