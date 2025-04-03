package com.decmoe47.todo.controller;

import com.decmoe47.todo.model.dto.*;
import com.decmoe47.todo.model.vo.R;
import com.decmoe47.todo.model.vo.TodoVO;
import com.decmoe47.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    // TODO: todo的权限认证，应对多人协作情况（现阶段只能获取自己的）
    public R<List<TodoVO>> getTodos(@RequestParam String listId) {
        return R.ok(todoService.getTodos(listId));
    }

    @PostMapping("/add")
    public R<TodoVO> addTodo(@RequestBody TodoAddDTO todoAddDTO) {
        return R.ok(todoService.addTodo(todoAddDTO));
    }

    @PostMapping("/delete")
    @PreAuthorize("@accessCheckService.ownsTodosAccess(authentication.principal.id, #todoDeleteDTOs.![id])")
    public R<Object> deleteTodo(@RequestBody List<TodoDeleteDTO> todoDeleteDTOs) {
        todoService.deleteTodos(todoDeleteDTOs);
        return R.ok();
    }

    @PostMapping("/update")
    @PreAuthorize("@accessCheckService.ownsTodosAccess(authentication.principal.id, #todoUpdateDTOs.![id])")
    public R<List<TodoVO>> updateTodos(@RequestBody List<TodoUpdateDTO> todoUpdateDTOs) {
        return R.ok(todoService.updateTodos(todoUpdateDTOs));
    }

    @PostMapping("/toggle")
    @PreAuthorize("@accessCheckService.ownsTodosAccess(authentication.principal.id, " +
            "T(java.util.Collections).singletonList(#todoToggleDTO.id))")
    public R<TodoVO> toggleTodo(@RequestBody TodoToggleDTO todoToggleDTO) {
        return R.ok(todoService.toggleTodo(todoToggleDTO));
    }

    @PostMapping("/move")
    public R<List<TodoVO>> moveTodos(@RequestBody List<TodoMoveDTO> todoMoveDTOs) {
        return R.ok(todoService.moveTodos(todoMoveDTOs));
    }
}
