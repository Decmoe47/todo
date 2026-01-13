package com.decmoe47.todo.model.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UserUpdateRequest(
    val id: Long,

    val name: String?,

    @field:Email
    val email: String?,

    @field:Size(min = 4, max = 4, message = "验证码为4位数字！")
    val verificationCode: String
)
