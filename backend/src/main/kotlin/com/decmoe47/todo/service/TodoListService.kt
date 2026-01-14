package com.decmoe47.todo.service

import com.decmoe47.todo.model.request.TodoListAddRequest
import com.decmoe47.todo.model.request.TodoListDeleteRequest
import com.decmoe47.todo.model.request.TodoListUpdateRequest
import com.decmoe47.todo.model.response.TodoListResponse

interface TodoListService {
    fun getAll(): List<TodoListResponse>

    fun add(request: TodoListAddRequest): TodoListResponse

    fun update(request: TodoListUpdateRequest): TodoListResponse

    fun delete(request: TodoListDeleteRequest)
}
