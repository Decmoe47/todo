package com.decmoe47.todo.controller

import com.decmoe47.todo.service.AuthService
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import tools.jackson.module.kotlin.jacksonObjectMapper

class AuthControllerValidationTest : FunSpec({
    val authService = mockk<AuthService>(relaxed = true)
    val controller = AuthController(authService)
    val objectMapper = jacksonObjectMapper()

    val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setValidator(validator)
        .build()

    test("register returns 400 when request body violates validation") {
        val payload = mapOf(
            "email" to "not-an-email",
            "password" to "secret",
            "name" to "abcdef",
            "verificationCode" to "1234"
        )

        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { authService.register(any()) }
    }

    test("login returns 400 when request body violates validation") {
        val payload = mapOf(
            "email" to "not-an-email",
            "password" to ""
        )

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { authService.login(any()) }
    }

    test("sendVerifyCode returns 400 when request body violates validation") {
        val payload = mapOf(
            "email" to "bad"
        )

        mockMvc.post("/api/auth/send-verify-code") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { authService.sendVerificationCode(any()) }
    }
})
