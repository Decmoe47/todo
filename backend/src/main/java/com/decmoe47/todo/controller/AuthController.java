package com.decmoe47.todo.controller;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.decmoe47.todo.constant.JwtConstants;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.model.dto.SendVerificationDTO;
import com.decmoe47.todo.model.dto.UserLoginDTO;
import com.decmoe47.todo.model.dto.UserRegisterDTO;
import com.decmoe47.todo.model.vo.R;
import com.decmoe47.todo.model.vo.UserVO;
import com.decmoe47.todo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${base64Secret}")
    private String base64Secret;

    @Operation(summary = "登录账号")
    @PostMapping("/login")
    public R<UserVO> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletResponse response) {
        UserVO userVO = authService.login(userLoginDTO);

        Map<String, Object> claims = HashMap.newHashMap(2);
        claims.put(JwtConstants.USER_ID, userVO.getId());
        claims.put("created", LocalDateTime.now());
        String token = JWTUtil.createToken(claims, base64Secret.getBytes());

        Cookie cookie = new Cookie(JwtConstants.TOKEN, token);
        cookie.setHttpOnly(true);
//        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 3600);
        response.addCookie(cookie);

        return R.ok(userVO);
    }

    @Operation(summary = "注册账号")
    @PostMapping("/register")
    public R<UserVO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        UserVO userVO = authService.register(userRegisterDTO);
        return R.ok(userVO);
    }

    @Operation(summary = "发送验证码")
    @PostMapping("/sendVerificationCode")
    public R<Object> sendVerifyCode(@RequestBody SendVerificationDTO sendVerificationDTO) {
        authService.sendVerifyCode(sendVerificationDTO.getEmail());
        return R.ok();
    }

    @Operation(summary = "检查登录状态")
    @GetMapping("/check")
    public R<UserVO> checkAuth(@CookieValue(name = JwtConstants.TOKEN, required = false) String token) {
        if (token == null) {
            return R.error(ErrorCodeEnum.UNAUTHORIZED);
        }

        try {
            JWT jwt = JWTUtil.parseToken(token);
            long userId = ((Number) jwt.getPayload(JwtConstants.USER_ID)).longValue();
            return R.ok(authService.getUser(userId));
        } catch (Exception e) {
            return R.error(ErrorCodeEnum.UNAUTHORIZED);
        }
    }
}
