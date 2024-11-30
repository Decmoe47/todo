package com.decmoe47.todo.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.jwt.JWTUtil;
import com.decmoe47.todo.constant.Constants;
import com.decmoe47.todo.constant.SessionAttributeKeys;
import com.decmoe47.todo.model.dto.*;
import com.decmoe47.todo.model.vo.LoginVO;
import com.decmoe47.todo.model.vo.R;
import com.decmoe47.todo.model.vo.UserVO;
import com.decmoe47.todo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "登录账号")
    @PostMapping("/login")
    public R<LoginVO> login(HttpServletRequest request, @RequestBody UserLoginDTO userLoginDTO) {
        UserVO userVO = userService.login(userLoginDTO, JakartaServletUtil.getClientIP(request));

        Map<String, Object> claims = HashMap.newHashMap(2);
        claims.put(SessionAttributeKeys.USER_ID, userVO.getId());
        claims.put("created", LocalDateTime.now());

        String token = JWTUtil.createToken(claims, Constants.BASE64_SECRET.getBytes(StandardCharsets.UTF_8));
        LoginVO loginVO = new LoginVO()
                .setCurrentUser(userVO)
                .setToken(token);

        return R.ok(loginVO);
    }

    @Operation(summary = "注册账号")
    @PostMapping("/register")
    public R<UserVO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        UserVO userVO = userService.register(userRegisterDTO);
        return R.ok(userVO);
    }

    @Operation(summary = "发送验证码")
    @PostMapping("/sendVerificationCode")
    public R<?> sendVerifyCode(@RequestBody SendVerificationDTO sendVerificationDTO) {
        userService.sendVerifyCode(sendVerificationDTO.getEmail());
        return R.ok();
    }

    @Operation(summary = "获取用户")
    @GetMapping("/{userId}")
    public R<UserVO> getUser(@PathVariable int userId) {
        return R.ok(userService.getUser(userId));
    }

    @GetMapping("/search")
    public R<List<UserVO>> searchUser(@RequestParam UserSearchDTO searchUserDTO) {
        return R.ok(userService.searchUser(searchUserDTO));
    }

    @PostMapping("/{userId}/update")
    public R<UserVO> updateUser(@PathVariable int userId, @RequestBody UserUpdateDTO updateUserDTO) {
        return R.ok(userService.updateUser(userId, updateUserDTO));
    }
}