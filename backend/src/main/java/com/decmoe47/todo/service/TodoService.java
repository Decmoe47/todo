package com.decmoe47.todo.service;

import com.decmoe47.todo.model.dto.TodoAddDTO;
import com.decmoe47.todo.model.dto.TodoDeleteDTO;
import com.decmoe47.todo.model.dto.TodoToggleDTO;
import com.decmoe47.todo.model.dto.TodoUpdateDTO;
import com.decmoe47.todo.model.vo.TodoVO;

import java.util.List;

public interface TodoService {

    List<TodoVO> getTodos(long userId, String listId, boolean inbox);

    TodoVO addTodo(TodoAddDTO todoAddDTO);

    void deleteTodo(TodoDeleteDTO todoDeleteDTO);

    List<TodoVO> updateTodos(List<TodoUpdateDTO> todoUpdateDTOS);

    TodoVO toggleTodo(TodoToggleDTO todoToggleDTO);
}
