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
import java.time.LocalDateTime

class TodoListControllerTest : FunSpec({
    val todoListService = mockk<TodoListService>()
    val controller = TodoListController(todoListService)
    val response = TodoListResponse(
        id = 1,
        name = "Work",
        inbox = false,
        createdBy = 1,
        createdAt = LocalDateTime.of(2024, 1, 1, 12, 0),
        updatedBy = null,
        updatedAt = null
    )

    beforeTest {
        clearMocks(todoListService)
    }

    test("getCustomTodoLists returns list") {
        every { todoListService.getAll() } returns listOf(response)

        controller.getAll().body?.data shouldBe listOf(response)

        verify { todoListService.getAll() }
    }

    test("addTodoList returns response") {
        val request = TodoListAddRequest(name = "Work")
        every { todoListService.add(request) } returns response

        controller.add(request).body?.data shouldBe response

        verify { todoListService.add(request) }
    }

    test("updateTodoList returns response") {
        val request = TodoListUpdateRequest(id = 1, name = "Work")
        every { todoListService.update(request) } returns response

        controller.update(request).body?.data shouldBe response

        verify { todoListService.update(request) }
    }

    test("deleteTodoList calls service") {
        val request = TodoListDeleteRequest(id = 1)
        every { todoListService.delete(request) } returns Unit

        controller.delete(request)

        verify { todoListService.delete(request) }
    }
})
