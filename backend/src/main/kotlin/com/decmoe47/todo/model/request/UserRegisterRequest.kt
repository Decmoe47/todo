package com.decmoe47.todo.model.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRegisterRequest(
    @field:Email
    @field:NotBlank(message = "邮箱不能为空！")
    val email: String,

    @field:NotBlank(message = "密码不能为空！")
    @field:Size(min = 6, message = "密码不允许少于6个字符！")
    val password: String,

    @field:NotBlank(message = "昵称不能为空！")
    val name: String,

    @field:NotBlank(message = "验证码不能为空")
    @field:Size(min = 4, max = 4, message = "验证码为4位数字！")
    val verificationCode: String,
)