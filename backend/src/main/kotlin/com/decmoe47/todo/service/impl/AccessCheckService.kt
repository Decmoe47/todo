package com.decmoe47.todo.service.impl

import com.decmoe47.todo.annotation.ReadOnlyTransactionalService
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.repository.TodoRepository
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.get

@ReadOnlyTransactionalService
class AccessCheckService(
    private val todoListRepository: TodoListRepository,
    private val todoRepository: TodoRepository,
    private val cacheManager: CacheManager,
) {
    @Cacheable(value = ["todoListAccess"], key = "#userId + '_' + #todoListId")
    fun ownsTodoListAccess(userId: Long, todoListId: Long): Boolean {
        val todoList = todoListRepository.first(todoListId)
        return todoList != null && todoList.createdBy == userId
    }

    fun ownsTodoAccess(userId: Long, todoId: Long): Boolean {
        val todoAccess = cacheManager.getCache("todoAccess")

        val cached = todoAccess?.get<Boolean>("${userId}_${todoId}")
        return if (cached != null) {
            true
        } else {
            val todo = todoRepository.first(todoId)

            val hasAccess = todo?.createdBy == userId
            todoAccess?.put("${userId}_${todoId}", hasAccess)
            hasAccess
        }
    }
}
