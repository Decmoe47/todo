package com.decmoe47.todo.repository

import com.decmoe47.todo.model.entity.TodoList

interface TodoListRepository {
    fun selectExcludingInbox(userId: Long): List<TodoList>

    fun getInbox(userId: Long): TodoList

    fun first(id: Long): TodoList?

    fun save(todoList: TodoList): TodoList

    fun update(todoList: TodoList): TodoList

    fun delete(id: Long)
}
