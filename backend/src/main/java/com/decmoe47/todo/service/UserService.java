package com.decmoe47.todo.service;

import com.decmoe47.todo.model.dto.UserSearchDTO;
import com.decmoe47.todo.model.dto.UserUpdateDTO;
import com.decmoe47.todo.model.vo.UserVO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    UserVO getUser(long userId);

    List<UserVO> searchUser(UserSearchDTO searchUserDTO);

    UserVO updateUser(long userId, UserUpdateDTO userUpdateDTO);

    UserVO getUserByToken(String token);
}
