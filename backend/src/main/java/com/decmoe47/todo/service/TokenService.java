package com.decmoe47.todo.service;

import com.decmoe47.todo.model.vo.AuthenticationTokensVO;
import org.springframework.security.core.Authentication;

public interface TokenService {

    AuthenticationTokensVO generate(Authentication authentication);

    Authentication parse(String token);

    boolean isValidated(String token);

    AuthenticationTokensVO refresh(String refreshToken);

    void invalidate(String token);
}
