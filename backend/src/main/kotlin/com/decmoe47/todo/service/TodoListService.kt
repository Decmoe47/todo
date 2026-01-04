package com.decmoe47.todo.service

import com.decmoe47.todo.model.request.TodoListAddRequest
import com.decmoe47.todo.model.request.TodoListDeleteRequest
import com.decmoe47.todo.model.request.TodoListUpdateRequest
import com.decmoe47.todo.model.response.TodoListResponse

interface TodoListService {
    fun getCustomTodoLists(): List<TodoListResponse>

    fun addTodoList(request: TodoListAddRequest): TodoListResponse

    fun updateTodoList(request: TodoListUpdateRequest): TodoListResponse

    fun deleteTodoList(request: TodoListDeleteRequest)
}
