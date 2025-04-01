package com.decmoe47.todo.service.impl;

import com.decmoe47.todo.model.entity.Todo;
import com.decmoe47.todo.model.entity.TodoList;
import com.decmoe47.todo.repository.TodoListRepository;
import com.decmoe47.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccessCheckService {

    private final TodoListRepository todoListRepo;
    private final TodoRepository todoRepo;
    private final CacheManager cacheManager;

    @Cacheable(value = "todoListAccess", key = "#userId + '_' + #todoListId")
    public boolean ownsTodoListAccess(long userId, String todoListId) {
        TodoList todoList = todoListRepo.findById(todoListId).orElse(null);
        return todoList != null && todoList.getCreatedBy() != null && todoList.getCreatedBy().getId() == userId;
    }

    public boolean ownsTodosAccess(long userId, List<Long> todoIds) {
        List<Boolean> result = new ArrayList<>();
        Cache cache = cacheManager.getCache("todoAccess");
        List<Long> missingIds = new ArrayList<>();

        // 尝试从缓存获取已有结果
        for (Long todoId : todoIds) {
            Boolean cached = (cache != null) ? cache.get(userId + "_" + todoId, Boolean.class) : null;
            if (cached != null) {
                result.add(cached);
            } else {
                missingIds.add(todoId);
            }
        }
        // 查询未缓存的todo并判断访问权限
        if (!missingIds.isEmpty()) {
            List<Todo> todos = todoRepo.findAllById(missingIds);
            Map<Long, Todo> todoMap = new HashMap<>();
            for (Todo todo : todos) {
                todoMap.put(todo.getId(), todo);
            }
            for (Long todoId : missingIds) {
                boolean hasAccess = false;
                if (todoMap.containsKey(todoId)) {
                    Todo todo = todoMap.get(todoId);
                    hasAccess = todo.getCreatedBy() != null && todo.getCreatedBy().getId() == userId;
                }

                result.add(hasAccess);
                if (cache != null) {
                    cache.put(userId + "_" + todoId, hasAccess);
                }
            }
        }
        return !result.contains(false);
    }
}
