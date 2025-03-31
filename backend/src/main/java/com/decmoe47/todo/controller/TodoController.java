package com.decmoe47.todo.controller;

import com.decmoe47.todo.model.dto.TodoAddDTO;
import com.decmoe47.todo.model.dto.TodoDeleteDTO;
import com.decmoe47.todo.model.dto.TodoToggleDTO;
import com.decmoe47.todo.model.dto.TodoUpdateDTO;
import com.decmoe47.todo.model.vo.R;
import com.decmoe47.todo.model.vo.TodoVO;
import com.decmoe47.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public R<List<TodoVO>> getTodos(@RequestParam long userId, @RequestParam String listId) {
        return R.ok(todoService.getTodos(userId, listId));
    }

    @PostMapping("/add")
    public R<TodoVO> addTodo(@RequestBody TodoAddDTO todoAddDTO) {
        return R.ok(todoService.addTodo(todoAddDTO));
    }

    @PostMapping("/delete")
    public R<Object> deleteTodo(@RequestBody TodoDeleteDTO todoDeleteDTO) {
        todoService.deleteTodo(todoDeleteDTO);
        return R.ok();
    }

    @PostMapping("/update")
    public R<List<TodoVO>> updateTodos(@RequestBody List<TodoUpdateDTO> todoUpdateDTOS) {
        return R.ok(todoService.updateTodos(todoUpdateDTOS));
    }

    @PostMapping("/toggle")
    public R<TodoVO> toggleTodo(@RequestBody TodoToggleDTO todoToggleDTO) {
        return R.ok(todoService.toggleTodo(todoToggleDTO));
    }
}
