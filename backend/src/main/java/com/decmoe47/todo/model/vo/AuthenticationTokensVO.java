package com.decmoe47.todo.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthenticationTokensVO {

    private String accessToken;
    private String refreshToken;
}
