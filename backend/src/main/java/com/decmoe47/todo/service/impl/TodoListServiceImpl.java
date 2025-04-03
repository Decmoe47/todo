package com.decmoe47.todo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.model.dto.TodoListAddDTO;
import com.decmoe47.todo.model.dto.TodoListDeleteDTO;
import com.decmoe47.todo.model.dto.TodoListUpdateDTO;
import com.decmoe47.todo.model.entity.Todo;
import com.decmoe47.todo.model.entity.TodoList;
import com.decmoe47.todo.model.entity.User;
import com.decmoe47.todo.model.vo.TodoListVO;
import com.decmoe47.todo.repository.TodoListRepository;
import com.decmoe47.todo.repository.TodoRepository;
import com.decmoe47.todo.repository.UserRepository;
import com.decmoe47.todo.service.TodoListService;
import com.decmoe47.todo.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TodoListServiceImpl implements TodoListService {

    private final TodoListRepository todoListRepo;
    private final TodoRepository todoRepo;
    private final UserRepository userRepo;
    private final CacheManager cacheManager;

    @Override
    public List<TodoListVO> getCustomTodoLists() {
        long userId = SecurityUtil.getCurrentUserId();
        List<TodoList> lists = todoListRepo.findByCreatedByIdAndInboxFalse(userId);
        return BeanUtil.copyToList(lists, TodoListVO.class);
    }

    @Override
    public TodoListVO addTodoList(TodoListAddDTO todoListAddDTO) {
        User user = userRepo.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.USER_NOT_FOUND));
        TodoList todoList = BeanUtil.toBean(todoListAddDTO, TodoList.class);
        todoList.setCreatedBy(user);
        todoList = todoListRepo.save(todoList);
        return BeanUtil.toBean(todoList, TodoListVO.class);
    }

    @Override
    public TodoListVO updateTodoList(TodoListUpdateDTO todoListUpdateDTO) {
        TodoList todoList = todoListRepo.findById(todoListUpdateDTO.getId())
                .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.TODO_LIST_NOT_FOUND));
        BeanUtil.copyProperties(todoListUpdateDTO, todoList);
        todoList = todoListRepo.save(todoList);
        return BeanUtil.toBean(todoList, TodoListVO.class);
    }

    @Transactional
    @Override
    public void deleteTodoList(TodoListDeleteDTO todoListDeleteDTO) {
        long userId = SecurityUtil.getCurrentUserId();
        List<Todo> todos = todoRepo.findByCreatedByIdAndBelongedList_Id(userId, todoListDeleteDTO.getId());    // TODO: 改用mybatis plus select单列
        List<Long> todoIds = todos.stream().map(Todo::getId).toList();

        todoRepo.deleteByBelongedList_Id(todoListDeleteDTO.getId());
        todoListRepo.deleteById(todoListDeleteDTO.getId());
        clearCache(todoListDeleteDTO.getId(), todoIds);
    }

    private void clearCache(String todoListId, List<Long> todoIds) {
        Cache todoListAccess = cacheManager.getCache("todoListAccess");
        if (todoListAccess != null) {
            todoListAccess.evict(SecurityUtil.getCurrentUserId() + "_" + todoListId);
        }

        Cache todoAccess = cacheManager.getCache("todoAccess");
        if (todoAccess != null) {
            for (Long todoId : todoIds) {
                todoAccess.evict(SecurityUtil.getCurrentUserId() + "_" + todoId);
            }
        }
    }
}
