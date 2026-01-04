package com.decmoe47.todo.service

import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.entity.AuditableEntity
import com.decmoe47.todo.model.entity.Todo
import com.decmoe47.todo.model.request.TodoListAddRequest
import com.decmoe47.todo.model.request.TodoListDeleteRequest
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.repository.TodoRepository
import com.decmoe47.todo.repository.UserRepository
import com.decmoe47.todo.service.impl.TodoListServiceImpl
import com.decmoe47.todo.util.SecurityUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

class TodoListServiceImplTest : FunSpec({
    val userId = 7L

    lateinit var todoListRepo: TodoListRepository
    lateinit var todoRepo: TodoRepository
    lateinit var userRepo: UserRepository
    lateinit var cacheManager: CacheManager
    lateinit var service: TodoListServiceImpl

    beforeTest {
        mockkObject(SecurityUtil)
        every { SecurityUtil.getCurrentUserId() } returns userId

        todoListRepo = mockk()
        todoRepo = mockk()
        userRepo = mockk()
        cacheManager = mockk()

        service = TodoListServiceImpl(
            todoListRepo = todoListRepo,
            todoRepo = todoRepo,
            userRepo = userRepo,
            cacheManager = cacheManager
        )
    }

    afterTest {
        unmockkObject(SecurityUtil)
    }

    test("addTodoList throws USER_NOT_FOUND when user does not exist") {
        every { userRepo.first(userId) } returns null

        val error = shouldThrow<ErrorResponseException> {
            service.addTodoList(TodoListAddRequest(name = "Inbox"))
        }

        error.errorCode shouldBe ErrorCode.USER_NOT_FOUND
    }

    test("deleteTodoList clears caches for list and todos") {
        val listId = 55L
        val todos = listOf(
            Todo(
                id = 1,
                content = "a",
                dueDate = null,
                belongedListId = listId,
                auditable = AuditableEntity(createdBy = userId)
            ),
            Todo(
                id = 2,
                content = "b",
                dueDate = null,
                belongedListId = listId,
                auditable = AuditableEntity(createdBy = userId)
            )
        )
        val todoListCache = mockk<Cache>(relaxed = true)
        val todoCache = mockk<Cache>(relaxed = true)

        every { todoRepo.select(listId) } returns todos
        every { todoRepo.deleteByBelongedListId(listId) } just runs
        every { todoListRepo.delete(listId) } just runs
        every { cacheManager.getCache("todoListAccess") } returns todoListCache
        every { cacheManager.getCache("todoAccess") } returns todoCache

        service.deleteTodoList(TodoListDeleteRequest(id = listId))

        verify(exactly = 1) { todoListCache.evict("${userId}_$listId") }
        verify(exactly = 1) { todoCache.evict("${userId}_1") }
        verify(exactly = 1) { todoCache.evict("${userId}_2") }
    }
})
