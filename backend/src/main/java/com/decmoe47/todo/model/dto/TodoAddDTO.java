package com.decmoe47.todo.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Data
public class TodoAddDTO {

    @NotBlank
    private String content;

    @Nullable
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueDate;

    @NotBlank
    private String belongedListId;
}
