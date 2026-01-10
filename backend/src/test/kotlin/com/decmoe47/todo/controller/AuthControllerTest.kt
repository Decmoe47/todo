package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.RefreshTokenRequest
import com.decmoe47.todo.model.request.SendVerifyCodeRequest
import com.decmoe47.todo.model.request.UserLoginRequest
import com.decmoe47.todo.model.request.UserRegisterRequest
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import com.decmoe47.todo.model.response.UserResponse
import com.decmoe47.todo.service.AuthService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders

class AuthControllerTest : FunSpec({
    val authService = mockk<AuthService>()
    val controller = AuthController(authService)

    beforeTest {
        clearMocks(authService)
    }

    test("login returns user response") {
        val request = UserLoginRequest(email = "user@test.com", password = "pw")
        val response = UserResponse(id = 1, email = "user@test.com", name = "User")
        every { authService.login(request) } returns response

        controller.login(request, null).data shouldBe response

        verify { authService.login(request) }
    }

    test("logout uses authorization header") {
        val request = mockk<HttpServletRequest>()
        every { request.getHeader(HttpHeaders.AUTHORIZATION) } returns "Bearer token"
        every { authService.logout("Bearer token") } returns Unit

        controller.logout(request)

        verify { authService.logout("Bearer token") }
    }

    test("register returns user response") {
        val request = UserRegisterRequest(
            email = "user@test.com",
            password = "pw",
            name = "User",
            verificationCode = "1234"
        )
        val response = UserResponse(id = 2, email = "user@test.com", name = "User")
        every { authService.register(request) } returns response

        controller.register(request).data shouldBe response

        verify { authService.register(request) }
    }

    test("sendVerifyCode calls service") {
        val request = SendVerifyCodeRequest(email = "user@test.com")
        every { authService.sendVerificationCode("user@test.com") } returns Unit

        controller.sendVerifyCode(request)

        verify { authService.sendVerificationCode("user@test.com") }
    }

    test("refreshToken returns tokens") {
        val request = RefreshTokenRequest(refreshToken = "refresh")
        val tokens = AuthenticationTokensResponse(accessToken = "access", refreshToken = "refresh")
        every { authService.refreshAccessToken("refresh") } returns tokens

        controller.refreshToken(request).data shouldBe tokens

        verify { authService.refreshAccessToken("refresh") }
    }
})
