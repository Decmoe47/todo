package com.decmoe47.todo.service;

import com.decmoe47.todo.model.dto.UserLoginDTO;
import com.decmoe47.todo.model.dto.UserRegisterDTO;
import com.decmoe47.todo.model.vo.AuthenticationTokensVO;
import com.decmoe47.todo.model.vo.UserVO;

public interface AuthService {

    UserVO login(UserLoginDTO userLoginDTO);

    void logout(String token);

    UserVO register(UserRegisterDTO userRegisterDTO);

    void sendVerificationCode(String email);

    AuthenticationTokensVO refreshAccessToken(String refreshToken);
}
