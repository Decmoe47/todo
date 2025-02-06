package com.decmoe47.todo.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDTO {

    @Email
    @NotBlank(message = "邮箱不能为空！")
    private String email;

    @NotBlank(message = "密码不能为空！")
    private String password;
}
