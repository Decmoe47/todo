package com.decmoe47.todo.service.impl

import com.decmoe47.todo.annotation.ReadOnlyTransactionalService
import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.entity.AuditableEntity
import com.decmoe47.todo.model.entity.TodoList
import com.decmoe47.todo.model.mapper.toTodoListResponse
import com.decmoe47.todo.model.request.TodoListAddRequest
import com.decmoe47.todo.model.request.TodoListDeleteRequest
import com.decmoe47.todo.model.request.TodoListUpdateRequest
import com.decmoe47.todo.model.response.TodoListResponse
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.repository.TodoRepository
import com.decmoe47.todo.repository.UserRepository
import com.decmoe47.todo.service.TodoListService
import com.decmoe47.todo.util.SecurityUtil
import org.springframework.cache.CacheManager
import org.springframework.transaction.annotation.Transactional

@ReadOnlyTransactionalService
class TodoListServiceImpl(
    private val todoListRepo: TodoListRepository,
    private val todoRepo: TodoRepository,
    private val userRepo: UserRepository,
    private val cacheManager: CacheManager,
) : TodoListService {
    override fun getAll(): List<TodoListResponse> {
        val userId = SecurityUtil.getCurrentUserId()
        val lists = todoListRepo.selectAll(userId)
        return lists.map { it.toTodoListResponse() }
    }

    @Transactional
    override fun add(request: TodoListAddRequest): TodoListResponse {
        val userId = SecurityUtil.getCurrentUserId()
        val user = userRepo.first(userId) ?: throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)

        val todoList = TodoList(
            name = request.name,
            auditable = AuditableEntity(createdBy = user.id),
        )
        return todoListRepo.save(todoList).toTodoListResponse()
    }

    @Transactional
    override fun update(request: TodoListUpdateRequest): TodoListResponse {
        val todoList = todoListRepo.first(request.id)
            ?: throw ErrorResponseException(ErrorCode.TODO_LIST_NOT_FOUND)
        val updated = todoList.copy(
            name = request.name,
            auditable = todoList.auditable.copy(updatedBy = SecurityUtil.getCurrentUserId()),
        )
        return todoListRepo.update(updated).toTodoListResponse()
    }

    @Transactional
    override fun delete(request: TodoListDeleteRequest) {
        val todos = todoRepo.select(request.id)
        val todoIds = todos.map { it.id }

        todoRepo.deleteByBelongedListId(request.id)
        todoListRepo.delete(request.id)
        clearCache(request.id, todoIds)
    }

    private fun clearCache(todoListId: Long, todoIds: List<Long>) {
        val userId = SecurityUtil.getCurrentUserId()
        val todoListAccess = cacheManager.getCache("todoListAccess")
        todoListAccess?.evict("${userId}_${todoListId}")

        val todoAccess = cacheManager.getCache("todoAccess")
        if (todoAccess != null) {
            todoIds.forEach { todoId ->
                todoAccess.evict("${userId}_${todoId}")
            }
        }
    }
}
