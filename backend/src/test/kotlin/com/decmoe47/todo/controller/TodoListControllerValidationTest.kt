package com.decmoe47.todo.controller

import com.decmoe47.todo.service.TodoListService
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

class TodoListControllerValidationTest : FunSpec({
    val todoListService = mockk<TodoListService>(relaxed = true)
    val controller = TodoListController(todoListService)
    val objectMapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
    }
    val messageConverter = MappingJackson2HttpMessageConverter(objectMapper)

    val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setValidator(validator)
        .setMessageConverters(messageConverter)
        .build()

    test("addTodoList returns 400 when request body violates validation") {
        val payload = mapOf(
            "name" to ""
        )

        mockMvc.post("/api/todoLists/add") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoListService.add(any()) }
    }

    test("updateTodoList returns 400 when request body violates validation") {
        val payload = mapOf(
            "id" to 1,
            "name" to ""
        )

        mockMvc.post("/api/todoLists/update") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoListService.update(any()) }
    }

    test("deleteTodoList returns 400 when request body violates validation") {
        val payload = mapOf(
            "id" to null
        )

        mockMvc.post("/api/todoLists/delete") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoListService.delete(any()) }
    }
})
