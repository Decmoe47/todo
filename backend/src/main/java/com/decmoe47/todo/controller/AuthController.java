package com.decmoe47.todo.controller;

import com.decmoe47.todo.model.dto.RefreshTokenDTO;
import com.decmoe47.todo.model.dto.SendVerifyCodeDTO;
import com.decmoe47.todo.model.dto.UserLoginDTO;
import com.decmoe47.todo.model.dto.UserRegisterDTO;
import com.decmoe47.todo.model.vo.AuthenticationTokensVO;
import com.decmoe47.todo.model.vo.R;
import com.decmoe47.todo.model.vo.UserVO;
import com.decmoe47.todo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录账号")
    @PostMapping("/login")
    public R<UserVO> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletResponse response) {
        UserVO userVO = authService.login(userLoginDTO);
        return R.ok(userVO);
    }

    @Operation(summary = "注销")
    @PostMapping("/logout")
    public R<Object> logout(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        authService.logout(token);
        return R.ok();
    }

    @Operation(summary = "注册账号")
    @PostMapping("/register")
    public R<UserVO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        UserVO userVO = authService.register(userRegisterDTO);
        return R.ok(userVO);
    }

    @Operation(summary = "发送验证码")
    @PostMapping("/send-verify-code")
    public R<Object> sendVerifyCode(@RequestBody SendVerifyCodeDTO sendVerifyCodeDTO) {
        authService.sendVerifyCode(sendVerifyCodeDTO.getEmail());
        return R.ok();
    }

    @Operation(summary = "刷新token")
    @PostMapping("/refresh-token")
    public R<AuthenticationTokensVO> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        return R.ok(authService.refreshAccessToken(refreshTokenDTO.getRefreshToken()));
    }
}
