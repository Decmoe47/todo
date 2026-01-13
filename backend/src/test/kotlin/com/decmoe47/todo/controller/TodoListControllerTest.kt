package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.TodoListAddRequest
import com.decmoe47.todo.model.request.TodoListDeleteRequest
import com.decmoe47.todo.model.request.TodoListUpdateRequest
import com.decmoe47.todo.model.response.TodoListResponse
import com.decmoe47.todo.service.TodoListService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.LocalDateTime

class TodoListControllerTest : FunSpec({
    val todoListService = mockk<TodoListService>()
    val controller = TodoListController(todoListService)
    val response = TodoListResponse(
        id = 1,
        name = "Work",
        inbox = false,
        createdBy = 1,
        createdAt = LocalDateTime(2024, 1, 1, 12, 0),
        updatedBy = null,
        updatedAt = null
    )

    beforeTest {
        clearMocks(todoListService)
    }

    test("getCustomTodoLists returns list") {
        every { todoListService.getCustomTodoLists() } returns listOf(response)

        controller.getCustomTodoLists().body?.data shouldBe listOf(response)

        verify { todoListService.getCustomTodoLists() }
    }

    test("addTodoList returns response") {
        val request = TodoListAddRequest(name = "Work")
        every { todoListService.addTodoList(request) } returns response

        controller.addTodoList(request).body?.data shouldBe response

        verify { todoListService.addTodoList(request) }
    }

    test("updateTodoList returns response") {
        val request = TodoListUpdateRequest(id = 1, name = "Work")
        every { todoListService.updateTodoList(request) } returns response

        controller.updateTodoList(request).body?.data shouldBe response

        verify { todoListService.updateTodoList(request) }
    }

    test("deleteTodoList calls service") {
        val request = TodoListDeleteRequest(id = 1)
        every { todoListService.deleteTodoList(request) } returns Unit

        controller.deleteTodoList(request)

        verify { todoListService.deleteTodoList(request) }
    }
})
