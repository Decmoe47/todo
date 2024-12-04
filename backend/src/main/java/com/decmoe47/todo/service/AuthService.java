package com.decmoe47.todo.service;

import com.decmoe47.todo.model.dto.UserLoginDTO;
import com.decmoe47.todo.model.dto.UserRegisterDTO;
import com.decmoe47.todo.model.vo.UserVO;

public interface AuthService {

    UserVO login(UserLoginDTO userLoginDTO);

    UserVO register(UserRegisterDTO userRegisterDTO);

    void sendVerifyCode(String email);

    UserVO getUser(long userId);
}
