package com.decmoe47.todo.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Data
public class TodoUpdateDTO {

    @NotNull
    private Long id;

    @NotBlank
    private String content;

    @NotNull
    private Boolean done;

    @Nullable
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueDate;

    @Nullable
    private String description;
}
