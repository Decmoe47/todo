package com.decmoe47.todo.controller

import com.decmoe47.todo.service.TodoService
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import tools.jackson.module.kotlin.jacksonObjectMapper

class TodoControllerValidationTest : FunSpec({
    val todoService = mockk<TodoService>(relaxed = true)
    val controller = TodoController(todoService)
    val objectMapper = jacksonObjectMapper()

    val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setValidator(validator)
        .build()

    test("addTodo returns 400 when request body violates validation") {
        val payload = mapOf(
            "content" to "",
            "belongedListId" to "1"
        )

        mockMvc.post("/api/todos/add") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoService.addTodo(any()) }
    }

    test("moveTodo returns 400 when request body violates validation") {
        val payload = mapOf(
            "id" to 1,
            "targetListId" to ""
        )

        mockMvc.post("/api/todos/move") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoService.moveTodo(any()) }
    }
})
