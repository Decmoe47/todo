package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.*
import com.decmoe47.todo.model.response.TodoResponse
import com.decmoe47.todo.service.TodoService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.LocalDateTime

class TodoControllerTest : FunSpec({
    val todoService = mockk<TodoService>()
    val controller = TodoController(todoService)
    val response = TodoResponse(
        id = 1,
        content = "todo",
        dueDate = LocalDateTime(2024, 1, 1, 12, 0),
        done = false,
        description = null,
        belongedListId = 2,
        createdBy = 1,
        createdAt = LocalDateTime(2024, 1, 1, 12, 0),
        updatedBy = null,
        updatedAt = null
    )

    beforeTest {
        clearMocks(todoService)
    }

    test("getTodos returns list") {
        every { todoService.getTodos("2") } returns listOf(response)

        controller.getTodos("2").body?.data shouldBe listOf(response)

        verify { todoService.getTodos("2") }
    }

    test("addTodo returns response") {
        val request = TodoAddRequest(content = "todo", dueDate = null, belongedListId = "2")
        every { todoService.addTodo(request) } returns response

        controller.addTodo(request).body?.data shouldBe response

        verify { todoService.addTodo(request) }
    }

    test("deleteTodo calls service") {
        val request = TodoDeleteRequest(id = 1, softDeleted = true)
        every { todoService.deleteTodo(request) } returns Unit

        controller.deleteTodo(request)

        verify { todoService.deleteTodo(request) }
    }

    test("updateTodos returns response") {
        val request = TodoUpdateRequest(
            id = 1,
            content = "todo",
            done = true,
            dueDate = null,
            description = "desc"
        )
        every { todoService.updateTodo(request) } returns response

        controller.updateTodos(request).body?.data shouldBe response

        verify { todoService.updateTodo(request) }
    }

    test("toggleTodo returns response") {
        val request = TodoToggleRequest(id = 1)
        every { todoService.toggleTodo(request) } returns response

        controller.toggleTodo(request).body?.data shouldBe response

        verify { todoService.toggleTodo(request) }
    }

    test("moveTodos returns response") {
        val request = TodoMoveRequest(id = 1, targetListId = "9")
        every { todoService.moveTodo(request) } returns response

        controller.moveTodos(request).body?.data shouldBe response

        verify { todoService.moveTodo(request) }
    }
})
