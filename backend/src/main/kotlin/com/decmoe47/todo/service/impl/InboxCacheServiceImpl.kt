package com.decmoe47.todo.service.impl

import com.decmoe47.todo.annotation.ReadOnlyTransactionalService
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.service.InboxCacheService
import org.springframework.cache.annotation.Cacheable

@ReadOnlyTransactionalService
class InboxCacheServiceImpl(private val todoListRepository: TodoListRepository) : InboxCacheService {

    @Cacheable(value = ["inbox"], key = "#userId")
    override fun getInboxId(userId: Long): Long = todoListRepository.getInbox(userId).id
}
