package com.decmoe47.todo.service.impl;

import com.decmoe47.todo.model.entity.TodoList;
import com.decmoe47.todo.repository.TodoListRepository;
import com.decmoe47.todo.service.InboxCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InboxCacheServiceImpl implements InboxCacheService {

    private final TodoListRepository todoListRepo;

    @Override
    @Cacheable(value = "inbox", key = "#userId")
    public String getInboxId(long userId) {
        TodoList inbox = todoListRepo.findFirstByCreatedByIdAndInboxTrue(userId);
        if (inbox == null) {
            throw new RuntimeException("Inbox not found");
        }
        return inbox.getId();
    }
}