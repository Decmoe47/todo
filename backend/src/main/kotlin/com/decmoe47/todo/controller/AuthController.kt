package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.RefreshTokenRequest
import com.decmoe47.todo.model.request.SendVerifyCodeRequest
import com.decmoe47.todo.model.request.UserLoginRequest
import com.decmoe47.todo.model.request.UserRegisterRequest
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import com.decmoe47.todo.model.response.Response
import com.decmoe47.todo.model.response.UserResponse
import com.decmoe47.todo.service.AuthService
import com.decmoe47.todo.util.R
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {
    @Operation(summary = "登录账号")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: UserLoginRequest): ResponseEntity<Response<UserResponse>> =
        R.ok(authService.login(request))

    @Operation(summary = "注销")
    @PostMapping("/logout")
    fun logout(servletRequest: HttpServletRequest): ResponseEntity<Response<Unit>> {
        val token = servletRequest.getHeader(HttpHeaders.AUTHORIZATION)
        authService.logout(token)
        return R.ok()
    }

    @Operation(summary = "注册账号")
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: UserRegisterRequest): ResponseEntity<Response<UserResponse>> =
        R.ok(authService.register(request))

    @Operation(summary = "发送验证码")
    @PostMapping("/send-verify-code")
    fun sendVerifyCode(@Valid @RequestBody request: SendVerifyCodeRequest): ResponseEntity<Response<Unit>> {
        authService.sendVerificationCode(request.email)
        return R.ok()
    }

    @Operation(summary = "刷新token")
    @PostMapping("/refresh-token")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<Response<AuthenticationTokensResponse>> =
        R.ok(authService.refreshAccessToken(request.refreshToken))
}
