package com.decmoe47.todo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.model.dto.TodoAddDTO;
import com.decmoe47.todo.model.dto.TodoDeleteDTO;
import com.decmoe47.todo.model.dto.TodoToggleDTO;
import com.decmoe47.todo.model.dto.TodoUpdateDTO;
import com.decmoe47.todo.model.entity.Todo;
import com.decmoe47.todo.model.entity.TodoList;
import com.decmoe47.todo.model.entity.User;
import com.decmoe47.todo.model.vo.TodoVO;
import com.decmoe47.todo.repository.TodoListRepository;
import com.decmoe47.todo.repository.TodoRepository;
import com.decmoe47.todo.repository.UserRepository;
import com.decmoe47.todo.service.TodoService;
import com.decmoe47.todo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepo;
    private final TodoListRepository todoListRepo;
    private final UserRepository userRepo;

    @Override
    public List<TodoVO> getTodos(long userId, String listId) {
        List<Todo> todos = todoRepo.findByCreatedByIdAndBelongedList_Id(userId, listId);
        return BeanUtil.copyToList(todos, TodoVO.class);
    }

    @Override
    public TodoVO addTodo(TodoAddDTO todoAddDTO) {
        User user = userRepo.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.USER_NOT_FOUND));
        Todo todo = BeanUtil.toBean(todoAddDTO, Todo.class);

        TodoList todoList = todoListRepo.findById(todoAddDTO.getBelongedListId())
                .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.TODO_LIST_NOT_FOUND));
        todo.setBelongedList(todoList);

        todo.setCreatedBy(user);
        todo = todoRepo.save(todo);
        return BeanUtil.toBean(todo, TodoVO.class);
    }

    @Override
    public void deleteTodo(TodoDeleteDTO todoDeleteDTO) {
        if (Boolean.TRUE.equals(todoDeleteDTO.getSoftDeleted())) {
            todoRepo.softDeleteById(todoDeleteDTO.getId());
        } else {
            todoRepo.deleteById(todoDeleteDTO.getId());
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
        todoRepo.saveAll(todosToSave);
        return BeanUtil.copyToList(todosToSave, TodoVO.class);
    }

    @Override
    public TodoVO toggleTodo(TodoToggleDTO todoToggleDTO) {
        Todo todo = todoRepo.findById(todoToggleDTO.getId())
                .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.TODO_NOT_FOUND));
        todo.setDone(!todo.isDone());
        todo = todoRepo.save(todo);
        return BeanUtil.toBean(todo, TodoVO.class);
    }
}
