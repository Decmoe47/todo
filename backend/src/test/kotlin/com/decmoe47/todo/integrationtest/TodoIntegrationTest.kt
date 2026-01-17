package com.decmoe47.todo.integrationtest

import com.decmoe47.todo.model.request.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("integration")
class TodoIntegrationTest : IntegrationTestBase() {

    @Test
    fun `full stack flow works`() {
        val (accessToken, _) = registerAndLogin("user@test.com")

        val todoListResponse = post(
            "/api/todoLists/add",
            TodoListAddRequest(name = "Work"),
            todoListResponseType,
            accessToken
        )
        val todoListId = requireNotNull(todoListResponse.data?.id)

        val todoResponse = post(
            "/api/todos/add",
            TodoAddRequest(content = "Write integration tests", dueDate = null, belongedListId = todoListId),
            todoResponseType,
            accessToken
        )
        val todoId = requireNotNull(todoResponse.data?.id)

        val todos = get(
            "/api/todos?listId=$todoListId",
            todoListResponseTypeList,
            accessToken
        )
        Assertions.assertThat(requireNotNull(todos.data).map { it.id }).contains(todoId)

        val toggled = post(
            "/api/todos/toggle",
            TodoToggleRequest(id = todoId),
            todoResponseType,
            accessToken
        )
        Assertions.assertThat(toggled.data?.done).isTrue()

        val deleted = post(
            "/api/todos/delete",
            TodoDeleteRequest(id = todoId, softDeleted = false),
            anyResponseType,
            accessToken
        )
        Assertions.assertThat(deleted.code).isEqualTo(0)
    }

    @Test
    fun `move and update todo across lists`() {
        val (accessToken, _) = registerAndLogin("move-todo@test.com")

        val listA = post(
            "/api/todoLists/add",
            TodoListAddRequest(name = "List A"),
            todoListResponseType,
            accessToken
        )
        val listB = post(
            "/api/todoLists/add",
            TodoListAddRequest(name = "List B"),
            todoListResponseType,
            accessToken
        )
        val listAId = requireNotNull(listA.data?.id)
        val listBId = requireNotNull(listB.data?.id)

        val todo = post(
            "/api/todos/add",
            TodoAddRequest(content = "Move me", dueDate = null, belongedListId = listAId),
            todoResponseType,
            accessToken
        )
        val todoId = requireNotNull(todo.data?.id)

        val moved = post(
            "/api/todos/move",
            TodoMoveRequest(id = todoId, targetListId = listBId),
            todoResponseType,
            accessToken
        )
        Assertions.assertThat(moved.data?.id).isEqualTo(todoId)

        val updated = post(
            "/api/todos/update",
            TodoUpdateRequest(
                id = todoId,
                content = "Moved and updated",
                done = true,
                dueDate = null,
                description = "Updated"
            ),
            todoResponseType,
            accessToken
        )
        Assertions.assertThat(updated.data?.done).isTrue()

        val listATodos = get(
            "/api/todos?listId=$listAId",
            todoListResponseTypeList,
            accessToken
        )
        val listBTodos = get(
            "/api/todos?listId=$listBId",
            todoListResponseTypeList,
            accessToken
        )
        Assertions.assertThat(requireNotNull(listATodos.data).map { it.id }).doesNotContain(todoId)
        Assertions.assertThat(requireNotNull(listBTodos.data).map { it.id }).contains(todoId)
    }
}
