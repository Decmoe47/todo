package com.decmoe47.todo.integrationtest

import com.decmoe47.todo.model.request.TodoAddRequest
import com.decmoe47.todo.model.request.TodoListAddRequest
import com.decmoe47.todo.model.request.TodoListUpdateRequest
import com.decmoe47.todo.model.request.TodoToggleRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

@Tag("integration")
class AccessControlIntegrationTest : IntegrationTestBase() {

    @Test
    fun `access is denied for other users`() {
        val (tokenA, _) = registerAndLogin("owner@test.com")
        val (tokenB, _) = registerAndLogin("intruder@test.com")

        val list = post(
            "/api/todoLists/add",
            TodoListAddRequest(name = "Private"),
            todoListResponseType,
            tokenA
        )
        val listId = requireNotNull(list.data?.id)
        val todo = post(
            "/api/todos/add",
            TodoAddRequest(content = "Private todo", dueDate = null, belongedListId = listId),
            todoResponseType,
            tokenA
        )
        val todoId = requireNotNull(todo.data?.id)

        val listUpdateStatus = postForStatus(
            "/api/todoLists/update",
            TodoListUpdateRequest(id = listId, name = "Hacked"),
            tokenB
        )
        Assertions.assertThat(listUpdateStatus).isEqualTo(HttpStatus.FORBIDDEN)

        val todoToggleStatus = postForStatus(
            "/api/todos/toggle",
            TodoToggleRequest(id = todoId),
            tokenB
        )
        Assertions.assertThat(todoToggleStatus).isEqualTo(HttpStatus.FORBIDDEN)
    }
}
