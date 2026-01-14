package com.decmoe47.todo.service.impl

import com.decmoe47.todo.annotation.ReadOnlyTransactionalService
import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.entity.AuditableEntity
import com.decmoe47.todo.model.entity.Todo
import com.decmoe47.todo.model.mapper.toTodoResponse
import com.decmoe47.todo.model.request.*
import com.decmoe47.todo.model.response.TodoResponse
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.repository.TodoRepository
import com.decmoe47.todo.repository.UserRepository
import com.decmoe47.todo.service.TodoService
import com.decmoe47.todo.util.SecurityUtil
import org.springframework.cache.CacheManager
import org.springframework.transaction.annotation.Transactional

@ReadOnlyTransactionalService
class TodoServiceImpl(
    private val todoRepo: TodoRepository,
    private val todoListRepo: TodoListRepository,
    private val userRepo: UserRepository,
    private val cacheManager: CacheManager,
) : TodoService {
    override fun getTodos(listId: Long): List<TodoResponse> {
        val todos = todoRepo.select(listId)
        return todos.map { it.toTodoResponse() }
    }

    @Transactional
    override fun addTodo(request: TodoAddRequest): TodoResponse {
        val userId = SecurityUtil.getCurrentUserId()
        val user = userRepo.first(userId) ?: throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)
        val todoList = todoListRepo.first(request.belongedListId)
            ?: throw ErrorResponseException(ErrorCode.TODO_LIST_NOT_FOUND)

        val todo = Todo(
            content = request.content,
            dueDate = request.dueDate,
            belongedListId = todoList.id,
            auditable = AuditableEntity(createdBy = user.id),
        )
        return todoRepo.save(todo).toTodoResponse()
    }

    @Transactional
    override fun deleteTodo(request: TodoDeleteRequest) {
        if (request.softDeleted) {
            todoRepo.softDelete(request.id)
        } else {
            todoRepo.delete(request.id)
        }
        clearCache(request.id)
    }

    @Transactional
    override fun updateTodo(request: TodoUpdateRequest): TodoResponse {
        val todo = todoRepo.first(request.id)
            ?: throw ErrorResponseException(ErrorCode.TODO_NOT_FOUND)
        val updatedTodo = todo.copy(
            content = request.content,
            done = request.done,
            dueDate = request.dueDate,
            description = request.description,
            auditable = todo.auditable.copy(updatedBy = SecurityUtil.getCurrentUserId()),
        )
        return todoRepo.update(updatedTodo).toTodoResponse()
    }

    @Transactional
    override fun toggleTodo(request: TodoToggleRequest): TodoResponse {
        val userId = SecurityUtil.getCurrentUserId()
        val todo = todoRepo.first(request.id)
            ?: throw ErrorResponseException(ErrorCode.TODO_NOT_FOUND)

        val updatedTodo = todoRepo.update(
            todo.copy(
                done = !todo.done,
                auditable = todo.auditable.copy(updatedBy = userId),
            )
        )
        return todoRepo.update(updatedTodo).toTodoResponse()
    }

    @Transactional
    override fun moveTodo(request: TodoMoveRequest): TodoResponse {
        val userId = SecurityUtil.getCurrentUserId()

        val todo = todoRepo.first(request.id)
            ?: throw ErrorResponseException(ErrorCode.TODO_NOT_FOUND)

        val todoList = todoListRepo.first(request.targetListId)
            ?: throw ErrorResponseException(ErrorCode.TODO_LIST_NOT_FOUND)

        val updatedTodo = todo.copy(
            belongedListId = todoList.id,
            auditable = todo.auditable.copy(updatedBy = userId),
        )

        return todoRepo.update(updatedTodo).toTodoResponse()
    }

    private fun clearCache(todoId: Long) {
        val todoAccess = cacheManager.getCache("todoAccess")
        todoAccess?.evict("${SecurityUtil.getCurrentUserId()}_${todoId}")
    }
}
