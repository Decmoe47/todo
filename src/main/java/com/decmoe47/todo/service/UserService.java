package com.decmoe47.todo.service;

import com.decmoe47.todo.model.dto.UserLoginDTO;
import com.decmoe47.todo.model.dto.UserRegisterDTO;
import com.decmoe47.todo.model.dto.UserSearchDTO;
import com.decmoe47.todo.model.dto.UserUpdateDTO;
import com.decmoe47.todo.model.vo.UserVO;

import java.util.List;

public interface UserService {

    UserVO login(UserLoginDTO userLoginDTO, String loginIp);

    UserVO register(UserRegisterDTO userRegisterDTO);

    void sendVerifyCode(String email);

    UserVO getUser(int userId);

    List<UserVO> searchUser(UserSearchDTO searchUserDTO);

    UserVO updateUser(int userId, UserUpdateDTO updateUserDTO);
}
