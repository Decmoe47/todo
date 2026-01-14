package com.decmoe47.todo.repository

import com.decmoe47.todo.model.entity.TodoList

interface TodoListRepository {
    fun selectAll(userId: Long): List<TodoList>

    fun first(id: Long): TodoList?

    fun save(todoList: TodoList): TodoList

    fun update(todoList: TodoList): TodoList

    fun delete(id: Long)
}
