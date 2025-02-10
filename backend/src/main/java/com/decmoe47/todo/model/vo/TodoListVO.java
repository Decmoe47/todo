package com.decmoe47.todo.model.vo;

import lombok.Data;

@Data
public class TodoListVO {

    private String id;
    private String name;

    private boolean inbox;

    private UserVO createdBy;
    private UserVO updatedBy;
}
