package com.decmoe47.todo.model.dto;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class UserSearchDTO {

    @Nullable
    private Integer id;

    @Nullable
    private String name;

    @Nullable
    private String email;
}
