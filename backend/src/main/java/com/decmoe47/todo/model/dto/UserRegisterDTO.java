package com.decmoe47.todo.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {

    @Email
    @NotBlank(message = "邮箱不能为空！")
    private String email;

    @NotBlank(message = "昵称不能为空！")
    private String name;

    @NotBlank(message = "密码不能为空！")
    @Size(min = 6, message = "密码不允许少于6个字符！")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 4, message = "验证码为4位数字！")
    private String verificationCode;
}
