package com.decmoe47.todo.integrationtest

import com.decmoe47.todo.model.request.TodoAddRequest
import com.decmoe47.todo.model.request.TodoListAddRequest
import com.decmoe47.todo.model.request.TodoListDeleteRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("integration")
class TodoListIntegrationTest : IntegrationTestBase() {

    @Test
    fun `delete todo list removes its todos`() {
        val (accessToken, _) = registerAndLogin("delete-list@test.com")

        val list = post(
            "/api/todoLists/add",
            TodoListAddRequest(name = "Temp"),
            todoListResponseType,
            accessToken
        )
        val listId = requireNotNull(list.data?.id)

        val todo = post(
            "/api/todos/add",
            TodoAddRequest(content = "Temp todo", dueDate = null, belongedListId = listId),
            todoResponseType,
            accessToken
        )
        val todoId = requireNotNull(todo.data?.id)

        val deleted = post(
            "/api/todoLists/delete",
            TodoListDeleteRequest(id = listId),
            anyResponseType,
            accessToken
        )
        Assertions.assertThat(deleted.code).isEqualTo(0)

        val todos = get(
            "/api/todos?listId=$listId",
            todoListResponseTypeList,
            accessToken
        )
        Assertions.assertThat(requireNotNull(todos.data).map { it.id }).doesNotContain(todoId)
    }
}
