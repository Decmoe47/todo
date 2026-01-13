package com.decmoe47.todo.model.mapper

import com.decmoe47.todo.model.entity.Todo
import com.decmoe47.todo.model.entity.TodoList
import com.decmoe47.todo.model.response.TodoListResponse
import com.decmoe47.todo.model.response.TodoResponse

fun TodoList.toTodoListResponse(): TodoListResponse {
    return TodoListResponse(
        id = id,
        name = name,
        inbox = inbox,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedBy = updatedBy,
        updatedAt = updatedAt,
    )
}

fun Todo.toTodoResponse(): TodoResponse {
    return TodoResponse(
        id = id,
        content = content,
        dueDate = dueDate,
        done = done,
        description = description,
        belongedListId = belongedListId,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedBy = updatedBy,
        updatedAt = updatedAt,
    )
}
