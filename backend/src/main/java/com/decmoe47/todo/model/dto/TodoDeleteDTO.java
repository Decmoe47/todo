package com.decmoe47.todo.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TodoDeleteDTO {

    @NotNull
    private Long id;

    @NotEmpty
    private Boolean softDeleted;
}
