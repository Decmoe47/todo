package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.TodoListAddRequest
import com.decmoe47.todo.model.request.TodoListDeleteRequest
import com.decmoe47.todo.model.request.TodoListUpdateRequest
import com.decmoe47.todo.model.response.R
import com.decmoe47.todo.model.response.TodoListResponse
import com.decmoe47.todo.service.TodoListService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/todoLists")
class TodoListController(private val todoListService: TodoListService) {
    @GetMapping("/custom")
    fun getCustomTodoLists(): R<List<TodoListResponse>> = R.ok(todoListService.getCustomTodoLists())

    @PostMapping("/add")
    fun addTodoList(@Valid @RequestBody request: TodoListAddRequest): R<TodoListResponse> =
        R.ok(todoListService.addTodoList(request))

    @PostMapping("/update")
    @PreAuthorize("@accessCheckService.ownsTodoListAccess(authentication.principal.id, #request.id)")
    fun updateTodoList(@Valid @RequestBody request: TodoListUpdateRequest): R<TodoListResponse> =
        R.ok(todoListService.updateTodoList(request))

    @PostMapping("/delete")
    @PreAuthorize("@accessCheckService.ownsTodoListAccess(authentication.principal.id, #request.id)")
    fun deleteTodoList(@Valid @RequestBody request: TodoListDeleteRequest): R<Unit> {
        todoListService.deleteTodoList(request)
        return R.ok()
    }
}
