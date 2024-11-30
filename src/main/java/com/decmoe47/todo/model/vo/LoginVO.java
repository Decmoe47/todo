package com.decmoe47.todo.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class LoginVO {

    private String token;
    private UserVO currentUser;
}
