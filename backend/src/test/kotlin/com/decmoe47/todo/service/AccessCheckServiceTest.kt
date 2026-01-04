package com.decmoe47.todo.service

import com.decmoe47.todo.model.entity.AuditableEntity
import com.decmoe47.todo.model.entity.Todo
import com.decmoe47.todo.model.entity.TodoList
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.repository.TodoRepository
import com.decmoe47.todo.service.impl.AccessCheckService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.get

class AccessCheckServiceTest : FunSpec({
    lateinit var todoListRepository: TodoListRepository
    lateinit var todoRepository: TodoRepository
    lateinit var cacheManager: CacheManager
    lateinit var cache: Cache
    lateinit var service: AccessCheckService

    beforeTest {
        todoListRepository = mockk()
        todoRepository = mockk()
        cacheManager = mockk()
        cache = mockk(relaxed = true)
        service = AccessCheckService(todoListRepository, todoRepository, cacheManager)
    }

    test("ownsTodoListAccess returns true when createdBy matches") {
        val list = TodoList(id = 1, name = "list", inbox = false, auditable = AuditableEntity(createdBy = 7))
        every { todoListRepository.first(1L) } returns list

        service.ownsTodoListAccess(userId = 7L, todoListId = 1L) shouldBe true
    }

    test("ownsTodoListAccess returns false when createdBy does not match") {
        val list = TodoList(id = 2, name = "list", inbox = false, auditable = AuditableEntity(createdBy = 1))
        every { todoListRepository.first(2L) } returns list

        service.ownsTodoListAccess(userId = 7L, todoListId = 2L) shouldBe false
    }

    test("ownsTodoAccess returns cached value without repository lookup") {
        every { cacheManager.getCache("todoAccess") } returns cache
        every { cache.get<Boolean>("7_10") } returns true

        service.ownsTodoAccess(userId = 7L, todoId = 10L) shouldBe true

        verify(exactly = 0) { todoRepository.first(any()) }
    }

    test("ownsTodoAccess caches repository result on cache miss") {
        val todo = Todo(
            id = 10,
            content = "c",
            dueDate = null,
            belongedListId = 1,
            auditable = AuditableEntity(createdBy = 7)
        )

        every { cacheManager.getCache("todoAccess") } returns cache
        every { cache.get<Boolean>("7_10") } returns null
        every { todoRepository.first(10L) } returns todo

        service.ownsTodoAccess(userId = 7L, todoId = 10L) shouldBe true

        verify(exactly = 1) { cache.put("7_10", true) }
    }
})
