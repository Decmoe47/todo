package com.decmoe47.todo.service

import com.decmoe47.todo.model.request.*
import com.decmoe47.todo.model.response.TodoResponse

interface TodoService {
    fun getTodos(listId: Long): List<TodoResponse>

    fun addTodo(request: TodoAddRequest): TodoResponse

    fun deleteTodo(request: TodoDeleteRequest)

    fun updateTodo(request: TodoUpdateRequest): TodoResponse

    fun toggleTodo(request: TodoToggleRequest): TodoResponse

    fun moveTodo(request: TodoMoveRequest): TodoResponse
}
