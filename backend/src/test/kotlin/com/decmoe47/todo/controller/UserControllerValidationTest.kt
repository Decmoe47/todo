package com.decmoe47.todo.controller

import com.decmoe47.todo.service.UserService
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import tools.jackson.module.kotlin.jacksonObjectMapper

class UserControllerValidationTest : FunSpec({
    val userService = mockk<UserService>(relaxed = true)
    val controller = UserController(userService)
    val objectMapper = jacksonObjectMapper()

    val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setValidator(validator)
        .build()

    test("updateUser returns 400 when request body violates validation") {
        val payload = mapOf(
            "id" to 1,
            "name" to "name",
            "email" to "bad",
            "verificationCode" to "12"
        )

        mockMvc.post("/api/users/1/update") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { userService.updateUser(any(), any()) }
    }
})
