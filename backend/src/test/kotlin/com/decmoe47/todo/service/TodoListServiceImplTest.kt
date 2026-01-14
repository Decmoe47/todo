package com.decmoe47.todo.service

import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.entity.AuditableEntity
import com.decmoe47.todo.model.entity.Todo
import com.decmoe47.todo.model.entity.TodoList
import com.decmoe47.todo.model.entity.User
import com.decmoe47.todo.model.request.TodoListAddRequest
import com.decmoe47.todo.model.request.TodoListDeleteRequest
import com.decmoe47.todo.model.request.TodoListUpdateRequest
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.repository.TodoRepository
import com.decmoe47.todo.repository.UserRepository
import com.decmoe47.todo.service.impl.TodoListServiceImpl
import com.decmoe47.todo.util.SecurityUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
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

    test("add throws USER_NOT_FOUND when user does not exist") {
        every { userRepo.first(userId) } returns null

        val error = shouldThrow<ErrorResponseException> {
            service.add(TodoListAddRequest(name = "Inbox"))
        }

        error.errorCode shouldBe ErrorCode.USER_NOT_FOUND
    }

    test("getAll returns mapped lists") {
        val list = TodoList(
            id = 2,
            name = "Work",
            inbox = false,
            auditable = AuditableEntity(createdBy = userId)
        )
        every { todoListRepo.selectAll(userId) } returns listOf(list)

        val result = service.getAll()

        result.find { it.id == 2L && it.name == "Work" } shouldNotBe null
    }

    test("add returns saved list") {
        val user = User(id = userId, email = "u@test.com", password = "pw", name = "u")
        val saved = TodoList(
            id = 3,
            name = "Inbox",
            inbox = false,
            auditable = AuditableEntity(createdBy = userId)
        )
        every { userRepo.first(userId) } returns user
        every { todoListRepo.save(any()) } returns saved

        val result = service.add(TodoListAddRequest(name = "Inbox"))

        result.id shouldBe 3L
    }

    test("update throws TODO_LIST_NOT_FOUND when list missing") {
        every { todoListRepo.first(4L) } returns null

        val error = shouldThrow<ErrorResponseException> {
            service.update(TodoListUpdateRequest(id = 4, name = "n"))
        }

        error.errorCode shouldBe ErrorCode.TODO_LIST_NOT_FOUND
    }

    test("update updates list name") {
        val list = TodoList(
            id = 4,
            name = "old",
            inbox = false,
            auditable = AuditableEntity(createdBy = userId)
        )
        val updated = list.copy(name = "new")
        every { todoListRepo.first(4L) } returns list
        every { todoListRepo.update(any()) } returns updated

        val result = service.update(TodoListUpdateRequest(id = 4, name = "new"))

        result.name shouldBe "new"
    }

    test("delete clears caches for list and todos") {
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

        service.delete(TodoListDeleteRequest(id = listId))

        verify(exactly = 1) { todoListCache.evict("${userId}_$listId") }
        verify(exactly = 1) { todoCache.evict("${userId}_1") }
        verify(exactly = 1) { todoCache.evict("${userId}_2") }
    }

    test("delete handles missing caches") {
        val listId = 77L
        every { todoRepo.select(listId) } returns emptyList()
        every { todoRepo.deleteByBelongedListId(listId) } returns Unit
        every { todoListRepo.delete(listId) } returns Unit
        every { cacheManager.getCache("todoListAccess") } returns null
        every { cacheManager.getCache("todoAccess") } returns null

        service.delete(TodoListDeleteRequest(id = listId))

        verify { todoRepo.deleteByBelongedListId(listId) }
        verify { todoListRepo.delete(listId) }
    }
})
