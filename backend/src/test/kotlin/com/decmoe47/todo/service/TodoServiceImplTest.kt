package com.decmoe47.todo.service

import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.entity.AuditableEntity
import com.decmoe47.todo.model.entity.Todo
import com.decmoe47.todo.model.entity.TodoList
import com.decmoe47.todo.model.entity.User
import com.decmoe47.todo.model.request.*
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.repository.TodoRepository
import com.decmoe47.todo.repository.UserRepository
import com.decmoe47.todo.service.impl.TodoServiceImpl
import com.decmoe47.todo.util.SecurityUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

class TodoServiceImplTest : FunSpec({
    val userId = 42L

    lateinit var todoRepo: TodoRepository
    lateinit var todoListRepo: TodoListRepository
    lateinit var userRepo: UserRepository
    lateinit var cacheManager: CacheManager
    lateinit var service: TodoServiceImpl

    beforeTest {
        mockkObject(SecurityUtil)
        every { SecurityUtil.getCurrentUserId() } returns userId

        todoRepo = mockk()
        todoListRepo = mockk()
        userRepo = mockk()
        cacheManager = mockk()

        service = TodoServiceImpl(
            todoRepo = todoRepo,
            todoListRepo = todoListRepo,
            userRepo = userRepo,
            cacheManager = cacheManager
        )
    }

    afterTest {
        unmockkObject(SecurityUtil)
    }

    test("getTodos resolves inbox list id via cache") {
        val inboxId = 100L
        val todo = Todo(
            id = 1,
            content = "content",
            dueDate = null,
            belongedListId = inboxId,
            auditable = AuditableEntity(createdBy = userId)
        )

        every { todoRepo.select(inboxId) } returns listOf(todo)

        val result = service.getTodos(100)

        result.shouldHaveSize(1)
        result.first().id shouldBe 1L
        verify(exactly = 1) { todoRepo.select(inboxId) }
    }

    test("addTodo throws USER_NOT_FOUND when user does not exist") {
        every { userRepo.first(userId) } returns null

        val request = TodoAddRequest(
            content = "test",
            dueDate = null,
            belongedListId = 1
        )

        val error = shouldThrow<ErrorResponseException> {
            service.addTodo(request)
        }

        error.errorCode shouldBe ErrorCode.USER_NOT_FOUND
    }

    test("addTodo throws TODO_LIST_NOT_FOUND when list is missing") {
        every { userRepo.first(userId) } returns User(id = userId, email = "u@test.com", password = "pw", name = "u")
        every { todoListRepo.first(1L) } returns null

        val error = shouldThrow<ErrorResponseException> {
            service.addTodo(TodoAddRequest(content = "test", dueDate = null, belongedListId = 1))
        }

        error.errorCode shouldBe ErrorCode.TODO_LIST_NOT_FOUND
    }

    test("addTodo returns response for existing user and list") {
        val user = User(id = userId, email = "u@test.com", password = "pw", name = "u")
        val list = TodoList(id = 3, name = "list", inbox = false, auditable = AuditableEntity(createdBy = userId))
        val saved = Todo(
            id = 9,
            content = "task",
            dueDate = null,
            belongedListId = list.id,
            auditable = AuditableEntity(createdBy = userId)
        )

        every { userRepo.first(userId) } returns user
        every { todoListRepo.first(3L) } returns list
        every { todoRepo.save(any()) } returns saved

        val result = service.addTodo(TodoAddRequest(content = "task", dueDate = null, belongedListId = 3))

        result.id shouldBe 9
        result.belongedListId shouldBe 3
    }

    test("deleteTodo uses soft delete and evicts cache when requested") {
        val cache = mockk<Cache>(relaxed = true)
        every { cacheManager.getCache("todoAccess") } returns cache
        every { todoRepo.softDelete(99L) } returns Unit

        service.deleteTodo(TodoDeleteRequest(id = 99L, softDeleted = true))

        verify(exactly = 1) { todoRepo.softDelete(99L) }
        verify(exactly = 0) { todoRepo.delete(any()) }
        verify(exactly = 1) { cache.evict("${userId}_99") }
    }

    test("deleteTodo hard deletes when requested and no cache") {
        every { cacheManager.getCache("todoAccess") } returns null
        every { todoRepo.delete(50L) } returns Unit

        service.deleteTodo(TodoDeleteRequest(id = 50L, softDeleted = false))

        verify { todoRepo.delete(50L) }
    }

    test("updateTodo throws TODO_NOT_FOUND when todo is missing") {
        every { todoRepo.first(8L) } returns null

        val error = shouldThrow<ErrorResponseException> {
            service.updateTodo(
                TodoUpdateRequest(
                    id = 8,
                    content = "x",
                    done = false,
                    dueDate = null,
                    description = null
                )
            )
        }

        error.errorCode shouldBe ErrorCode.TODO_NOT_FOUND
    }

    test("updateTodo returns updated response") {
        val todo = Todo(
            id = 8,
            content = "old",
            dueDate = null,
            done = false,
            description = null,
            belongedListId = 2,
            auditable = AuditableEntity(createdBy = userId)
        )
        every { todoRepo.first(8L) } returns todo

        val result = service.updateTodo(
            TodoUpdateRequest(id = 8, content = "new", done = true, dueDate = null, description = "d")
        )

        result.content shouldBe "new"
        result.done shouldBe true
        result.description shouldBe "d"
    }

    test("toggleTodo throws TODO_NOT_FOUND when todo is missing") {
        every { todoRepo.first(1L) } returns null

        val error = shouldThrow<ErrorResponseException> {
            service.toggleTodo(TodoToggleRequest(id = 1))
        }

        error.errorCode shouldBe ErrorCode.TODO_NOT_FOUND
    }

    test("toggleTodo updates done flag") {
        val todo = Todo(
            id = 2,
            content = "task",
            dueDate = null,
            done = false,
            description = null,
            belongedListId = 2,
            auditable = AuditableEntity(createdBy = userId)
        )
        val updated = todo.copy(done = true)
        every { todoRepo.first(2L) } returns todo
        every { todoRepo.update(any()) } returns updated

        val result = service.toggleTodo(TodoToggleRequest(id = 2))

        result.done shouldBe true
    }

    test("moveTodo throws INVALID_REQUEST_PARAMS when target list id is invalid") {
        every { todoRepo.first(1L) } returns Todo(
            id = 1,
            content = "task",
            dueDate = null,
            done = false,
            description = null,
            belongedListId = 2,
            auditable = AuditableEntity(createdBy = userId)
        )

        val error = shouldThrow<ErrorResponseException> {
            service.moveTodo(TodoMoveRequest(id = 1, targetListId = -1))
        }

        error.errorCode shouldBe ErrorCode.INVALID_REQUEST_PARAMS
    }

    test("moveTodo throws TODO_LIST_NOT_FOUND when target list missing") {
        every { todoRepo.first(1L) } returns Todo(
            id = 1,
            content = "task",
            dueDate = null,
            done = false,
            description = null,
            belongedListId = 2,
            auditable = AuditableEntity(createdBy = userId)
        )
        every { todoListRepo.first(3L) } returns null

        val error = shouldThrow<ErrorResponseException> {
            service.moveTodo(TodoMoveRequest(id = 1, targetListId = 999))
        }

        error.errorCode shouldBe ErrorCode.TODO_LIST_NOT_FOUND
    }

    test("moveTodo returns response for valid target list") {
        val todo = Todo(
            id = 1,
            content = "task",
            dueDate = null,
            done = false,
            description = null,
            belongedListId = 2,
            auditable = AuditableEntity(createdBy = userId)
        )
        every { todoRepo.first(1L) } returns todo
        every { todoListRepo.first(5L) } returns TodoList(
            id = 5,
            name = "target",
            inbox = false,
            auditable = AuditableEntity(createdBy = userId)
        )

        val result = service.moveTodo(TodoMoveRequest(id = 1, targetListId = 5))

        result.belongedListId shouldBe 5
    }

    test("getTodos throws INVALID_REQUEST_PARAMS when listId is invalid") {
        val error = shouldThrow<ErrorResponseException> {
            service.getTodos(-1)
        }

        error.errorCode shouldBe ErrorCode.INVALID_REQUEST_PARAMS
    }
})
