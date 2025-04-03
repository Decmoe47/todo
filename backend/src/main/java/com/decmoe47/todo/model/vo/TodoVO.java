package com.decmoe47.todo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoVO {

    private long id;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueDate;

    private boolean done;
    private String description;
    private TodoListVO belongedList;

    private UserVO createdBy;
    private UserVO updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;
}
