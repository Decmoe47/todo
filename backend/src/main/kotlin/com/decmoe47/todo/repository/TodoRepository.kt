package com.decmoe47.todo.repository

import com.decmoe47.todo.model.entity.Todo

interface TodoRepository {
    fun select(listId: Long): List<Todo>

    fun first(id: Long): Todo?

    fun selectIn(ids: List<Long>): List<Todo>

    fun save(todo: Todo): Todo

    fun update(todo: Todo): Todo

    fun delete(id: Long)

    fun deleteByBelongedListId(belongedListId: Long)

    fun softDelete(id: Long)
}
