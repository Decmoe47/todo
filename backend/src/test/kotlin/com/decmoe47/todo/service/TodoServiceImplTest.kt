package com.decmoe47.todo.service

import com.decmoe47.todo.constant.TodoConstants
import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.entity.AuditableEntity
import com.decmoe47.todo.model.entity.Todo
import com.decmoe47.todo.model.request.TodoAddRequest
import com.decmoe47.todo.model.request.TodoDeleteRequest
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.repository.TodoRepository
import com.decmoe47.todo.repository.UserRepository
import com.decmoe47.todo.service.impl.TodoServiceImpl
import com.decmoe47.todo.util.SecurityUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
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

class TodoServiceImplTest : FunSpec({
    val userId = 42L

    lateinit var todoRepo: TodoRepository
    lateinit var todoListRepo: TodoListRepository
    lateinit var userRepo: UserRepository
    lateinit var inboxCacheService: InboxCacheService
    lateinit var cacheManager: CacheManager
    lateinit var service: TodoServiceImpl

    beforeTest {
        mockkObject(SecurityUtil)
        every { SecurityUtil.getCurrentUserId() } returns userId

        todoRepo = mockk()
        todoListRepo = mockk()
        userRepo = mockk()
        inboxCacheService = mockk()
        cacheManager = mockk()

        service = TodoServiceImpl(
            todoRepo = todoRepo,
            todoListRepo = todoListRepo,
            userRepo = userRepo,
            inboxCacheService = inboxCacheService,
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

        every { inboxCacheService.getInboxId(userId) } returns inboxId
        every { todoRepo.select(inboxId) } returns listOf(todo)

        val result = service.getTodos(TodoConstants.INBOX)

        result.shouldHaveSize(1)
        result.first().id shouldBe 1L
        verify(exactly = 1) { todoRepo.select(inboxId) }
    }

    test("addTodo throws USER_NOT_FOUND when user does not exist") {
        every { userRepo.first(userId) } returns null

        val request = TodoAddRequest(
            content = "test",
            dueDate = null,
            belongedListId = "1"
        )

        val error = shouldThrow<ErrorResponseException> {
            service.addTodo(request)
        }

        error.errorCode shouldBe ErrorCode.USER_NOT_FOUND
    }

    test("deleteTodo uses soft delete and evicts cache when requested") {
        val cache = mockk<Cache>(relaxed = true)
        every { cacheManager.getCache("todoAccess") } returns cache
        every { todoRepo.softDelete(99L) } just runs

        service.deleteTodo(TodoDeleteRequest(id = 99L, softDeleted = true))

        verify(exactly = 1) { todoRepo.softDelete(99L) }
        verify(exactly = 0) { todoRepo.delete(any()) }
        verify(exactly = 1) { cache.evict("${userId}_99") }
    }
})
