package com.decmoe47.todo.service

import com.decmoe47.todo.model.entity.TodoList
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.service.impl.InboxCacheServiceImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class InboxCacheServiceImplTest : FunSpec({
    test("getInboxId returns inbox list id") {
        val todoListRepository = mockk<TodoListRepository>()
        every { todoListRepository.getInbox(7) } returns TodoList(id = 99, name = "Inbox", inbox = true)

        val service = InboxCacheServiceImpl(todoListRepository)

        service.getInboxId(7) shouldBe 99
        verify(exactly = 1) { todoListRepository.getInbox(7) }
    }
})
