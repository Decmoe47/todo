package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.*
import com.decmoe47.todo.model.response.R
import com.decmoe47.todo.model.response.TodoResponse
import com.decmoe47.todo.service.TodoService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todos")
class TodoController(private val todoService: TodoService) {
    @GetMapping
    fun getTodos(@RequestParam listId: String): R<List<TodoResponse>> = R.ok(todoService.getTodos(listId))

    @PostMapping("/add")
    fun addTodo(@Valid @RequestBody request: TodoAddRequest): R<TodoResponse> =
        R.ok(todoService.addTodo(request))

    @PostMapping("/delete")
    @PreAuthorize("@accessCheckService.ownsTodoAccess(authentication.principal.id, #request.id)")
    fun deleteTodo(@Valid @RequestBody request: TodoDeleteRequest): R<Unit> {
        todoService.deleteTodo(request)
        return R.ok()
    }

    @PostMapping("/update")
    @PreAuthorize("@accessCheckService.ownsTodoAccess(authentication.principal.id, #request.id)")
    fun updateTodos(@Valid @RequestBody request: TodoUpdateRequest): R<TodoResponse> =
        R.ok(todoService.updateTodo(request))

    @PostMapping("/toggle")
    @PreAuthorize("@accessCheckService.ownsTodoAccess(authentication.principal.id, #request.id)")
    fun toggleTodo(@Valid @RequestBody request: TodoToggleRequest): R<TodoResponse> =
        R.ok(todoService.toggleTodo(request))

    @PostMapping("/move")
    fun moveTodos(@Valid @RequestBody request: TodoMoveRequest): R<TodoResponse> =
        R.ok(todoService.moveTodo(request))
}
