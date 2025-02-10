package com.decmoe47.todo.service;

import com.decmoe47.todo.model.dto.TodoListAddDTO;
import com.decmoe47.todo.model.dto.TodoListDeleteDTO;
import com.decmoe47.todo.model.dto.TodoListUpdateDTO;
import com.decmoe47.todo.model.vo.TodoListVO;

import java.util.List;

public interface TodoListService {

    List<TodoListVO> getCustomTodoLists(long userId);

    TodoListVO addTodoList(TodoListAddDTO todoListAddDTO);

    TodoListVO updateTodoList(TodoListUpdateDTO todoListUpdateDTO);

    void deleteTodoList(TodoListDeleteDTO todoListDeleteDTO);
}
