package com.decmoe47.todo.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class UserUpdateDTO {

    @Nullable
    private String name;

    @Email
    @Nullable
    private String newEmail;

    @Nullable
    @Size(min = 4, max = 4, message = "验证码为4位数字！")
    private String verificationCode;
}
