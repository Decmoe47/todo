package com.decmoe47.todo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TodoListUpdateDTO {

    @NotNull
    private String id;

    @NotBlank
    private String name;
}
