package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.RefreshTokenRequest
import com.decmoe47.todo.model.request.SendVerifyCodeRequest
import com.decmoe47.todo.model.request.UserLoginRequest
import com.decmoe47.todo.model.request.UserRegisterRequest
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import com.decmoe47.todo.model.response.R
import com.decmoe47.todo.model.response.UserResponse
import com.decmoe47.todo.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {
    @Operation(summary = "登录账号")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: UserLoginRequest, response: HttpServletResponse?): R<UserResponse> =
        R.ok(authService.login(request))

    @Operation(summary = "注销")
    @PostMapping("/logout")
    fun logout(servletRequest: HttpServletRequest): R<Unit> {
        val token = servletRequest.getHeader(HttpHeaders.AUTHORIZATION)
        authService.logout(token)
        return R.ok()
    }

    @Operation(summary = "注册账号")
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: UserRegisterRequest): R<UserResponse> =
        R.ok(authService.register(request))

    @Operation(summary = "发送验证码")
    @PostMapping("/send-verify-code")
    fun sendVerifyCode(@Valid @RequestBody request: SendVerifyCodeRequest): R<Unit> {
        authService.sendVerificationCode(request.email)
        return R.ok()
    }

    @Operation(summary = "刷新token")
    @PostMapping("/refresh-token")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): R<AuthenticationTokensResponse> =
        R.ok(authService.refreshAccessToken(request.refreshToken))
}
