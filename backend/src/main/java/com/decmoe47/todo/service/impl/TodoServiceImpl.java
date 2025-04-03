package com.decmoe47.todo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import com.decmoe47.todo.constant.TodoConstants;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.model.dto.*;
import com.decmoe47.todo.model.entity.Todo;
import com.decmoe47.todo.model.entity.TodoList;
import com.decmoe47.todo.model.entity.User;
import com.decmoe47.todo.model.vo.TodoVO;
import com.decmoe47.todo.repository.TodoListRepository;
import com.decmoe47.todo.repository.TodoRepository;
import com.decmoe47.todo.repository.UserRepository;
import com.decmoe47.todo.service.InboxCacheService;
import com.decmoe47.todo.service.TodoService;
import com.decmoe47.todo.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepo;
    private final TodoListRepository todoListRepo;
    private final UserRepository userRepo;
    private final InboxCacheService inboxCacheService;
    private final CacheManager cacheManager;

    @Override
    public List<TodoVO> getTodos(String listId) {
        long userId = SecurityUtil.getCurrentUserId();
        List<Todo> todos;
        if (TodoConstants.INBOX.equals(listId)) {
            listId = inboxCacheService.getInboxId(userId);
        }
        todos = todoRepo.findByCreatedByIdAndBelongedList_Id(userId, listId);
        return BeanUtil.copyToList(todos, TodoVO.class);
    }

    @Override
    public TodoVO addTodo(TodoAddDTO todoAddDTO) {
        User user = userRepo.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.USER_NOT_FOUND));
        Todo todo = BeanUtil.toBean(todoAddDTO, Todo.class);

        String listId = TodoConstants.INBOX.equals(todoAddDTO.getBelongedListId())
                ? inboxCacheService.getInboxId(user.getId())
                : todoAddDTO.getBelongedListId();
        TodoList todoList = todoListRepo.findById(listId)
                .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.TODO_LIST_NOT_FOUND));
        todo.setBelongedList(todoList);

        todo.setCreatedBy(user);
        todo = todoRepo.save(todo);
        return BeanUtil.toBean(todo, TodoVO.class);
    }

    @Transactional
    @Override
    public void deleteTodos(List<TodoDeleteDTO> todoDeleteDTOs) {
        for (TodoDeleteDTO todoDeleteDTO : todoDeleteDTOs) {
            if (Boolean.TRUE.equals(todoDeleteDTO.getSoftDeleted())) {
                todoRepo.softDeleteById(todoDeleteDTO.getId());
            } else {
                todoRepo.deleteById(todoDeleteDTO.getId());
            }
            cleaCache(todoDeleteDTO.getId());
        }
    }

    @Override
    public List<TodoVO> updateTodos(List<TodoUpdateDTO> todoUpdateDTOs) {
        List<Todo> todosToSave = new ArrayList<>();
        for (TodoUpdateDTO todoUpdateDTO : todoUpdateDTOs) {
            Todo todo = todoRepo.findById(todoUpdateDTO.getId())
                    .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.TODO_NOT_FOUND));
            BeanUtil.copyProperties(todoUpdateDTO, todo);
            todosToSave.add(todo);
        }

        if (!todosToSave.isEmpty()) {
            todoRepo.saveAll(todosToSave);
            return BeanUtil.copyToList(todosToSave, TodoVO.class);
        } else {
            return ListUtil.empty();
        }
    }

    @Override
    public TodoVO toggleTodo(TodoToggleDTO todoToggleDTO) {
        Todo todo = todoRepo.findById(todoToggleDTO.getId())
                .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.TODO_NOT_FOUND));
        todo.setDone(!todo.isDone());
        todo = todoRepo.save(todo);
        return BeanUtil.toBean(todo, TodoVO.class);
    }

    private void cleaCache(long todoId) {
        Cache todoAccess = cacheManager.getCache("todoAccess");
        if (todoAccess != null) {
            todoAccess.evict(SecurityUtil.getCurrentUserId() + "_" + todoId);
        }
    }

    @Override
    public List<TodoVO> moveTodos(List<TodoMoveDTO> todoMoveDTOs) {
        List<Todo> todosToSave = new ArrayList<>();
        for (TodoMoveDTO todoMoveDTO : todoMoveDTOs) {
            Todo todo = todoRepo.findById(todoMoveDTO.getId())
                    .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.TODO_NOT_FOUND));

            String listId = TodoConstants.INBOX.equals(todoMoveDTO.getTargetListId())
                    ? inboxCacheService.getInboxId(SecurityUtil.getCurrentUserId())
                    : todoMoveDTO.getTargetListId();
            TodoList todoList = todoListRepo.findById(listId)
                    .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.TODO_LIST_NOT_FOUND));      // TODO: 改用mybatis plus直接update

            todo.setBelongedList(todoList);
            todosToSave.add(todo);
        }

        if (!todosToSave.isEmpty()) {
            todoRepo.saveAll(todosToSave);
            return BeanUtil.copyToList(todosToSave, TodoVO.class);
        } else {
            return ListUtil.empty();
        }
    }
}
