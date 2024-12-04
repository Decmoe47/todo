package com.decmoe47.todo.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TodoToggleDTO {

    @NotNull
    private Long id;
}
