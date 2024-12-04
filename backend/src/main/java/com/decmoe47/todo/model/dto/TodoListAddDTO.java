package com.decmoe47.todo.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TodoListAddDTO {

    @NotBlank
    private String name;
}
