package com.decmoe47.todo.controller;

import com.decmoe47.todo.model.dto.UserSearchDTO;
import com.decmoe47.todo.model.dto.UserUpdateDTO;
import com.decmoe47.todo.model.vo.R;
import com.decmoe47.todo.model.vo.UserVO;
import com.decmoe47.todo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取用户")
    @GetMapping("/{userId}")
    public R<UserVO> getUser(@PathVariable Long userId) {
        return R.ok(userService.getUser(userId));
    }

    @GetMapping("/by-token")
    public R<UserVO> getUserByToken(@RequestParam String token) {
        return R.ok(userService.getUserByToken(token));
    }

    @GetMapping("/search")
    public R<List<UserVO>> searchUser(@RequestParam UserSearchDTO searchUserDTO) {
        return R.ok(userService.searchUser(searchUserDTO));
    }

    @PostMapping("/{userId}/update")
    public R<UserVO> updateUser(@PathVariable Long userId, @RequestBody UserUpdateDTO userUpdateDTO) {
        return R.ok(userService.updateUser(userId, userUpdateDTO));
    }
}