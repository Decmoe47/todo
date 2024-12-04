package com.decmoe47.todo.controller;

import com.decmoe47.todo.model.dto.TodoListAddDTO;
import com.decmoe47.todo.model.dto.TodoListDeleteDTO;
import com.decmoe47.todo.model.dto.TodoListUpdateDTO;
import com.decmoe47.todo.model.vo.R;
import com.decmoe47.todo.model.vo.TodoListVO;
import com.decmoe47.todo.service.TodoListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/todoLists")
public class TodoListController {

    private final TodoListService todoListService;

    @GetMapping
    public R<List<TodoListVO>> getTodoLists(@RequestParam long userId) {
        return R.ok(todoListService.getTodoLists(userId));
    }

    @PostMapping("/add")
    public R<TodoListVO> addTodoList(@RequestBody TodoListAddDTO todoListAddDTO) {
        return R.ok(todoListService.addTodoList(todoListAddDTO));
    }

    @PostMapping("/update")
    public R<TodoListVO> updateTodoList(@RequestBody TodoListUpdateDTO todoListUpdateDTO) {
        return R.ok(todoListService.updateTodoList(todoListUpdateDTO));
    }

    @PostMapping("/delete")
    public R<Object> deleteTodoList(@RequestBody TodoListDeleteDTO todoListDeleteDTO) {
        todoListService.deleteTodoList(todoListDeleteDTO);
        return R.ok();
    }
}
