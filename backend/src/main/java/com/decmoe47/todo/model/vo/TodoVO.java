package com.decmoe47.todo.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoVO {

    private long id;
    private String content;
    private LocalDateTime dueDate;
    private boolean done;
    private String description;
    private TodoListVO belongedList;

    private UserVO createdBy;
    private UserVO updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;
}
