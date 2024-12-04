package com.decmoe47.todo.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TodoListVO {

    private String id;
    private String name;

    @JsonProperty("isInbox")
    private boolean isInbox;

    private UserVO createdBy;
    private UserVO updatedBy;
}
