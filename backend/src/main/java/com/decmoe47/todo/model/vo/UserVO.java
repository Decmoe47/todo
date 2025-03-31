package com.decmoe47.todo.model.vo;

import lombok.Data;

@Data
public class UserVO {

    private long id;
    private String email;
    private String name;

    private AuthenticationTokensVO tokens;
}
