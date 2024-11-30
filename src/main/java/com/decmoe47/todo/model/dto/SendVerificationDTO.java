package com.decmoe47.todo.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendVerificationDTO {

    @NotBlank
    @Email
    private String email;
}
