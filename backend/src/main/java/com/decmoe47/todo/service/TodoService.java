package com.decmoe47.todo.service;

import com.decmoe47.todo.model.dto.*;
import com.decmoe47.todo.model.vo.TodoVO;

import java.util.List;

public interface TodoService {

    List<TodoVO> getTodos(String listId);

    TodoVO addTodo(TodoAddDTO todoAddDTO);

    void deleteTodos(List<TodoDeleteDTO> todoDeleteDTOs);

    List<TodoVO> updateTodos(List<TodoUpdateDTO> todoUpdateDTOS);

    TodoVO toggleTodo(TodoToggleDTO todoToggleDTO);

    List<TodoVO> moveTodos(List<TodoMoveDTO> todoMoveDTOs);
}
