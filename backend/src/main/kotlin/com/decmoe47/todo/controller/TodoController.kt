package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.*
import com.decmoe47.todo.model.response.Response
import com.decmoe47.todo.model.response.TodoResponse
import com.decmoe47.todo.service.TodoService
import com.decmoe47.todo.util.R
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todos")
class TodoController(private val todoService: TodoService) {
    @GetMapping
    fun getTodos(@RequestParam listId: Long): ResponseEntity<Response<List<TodoResponse>>> =
        R.ok(todoService.getTodos(listId))

    @PostMapping("/add")
    fun addTodo(@Valid @RequestBody request: TodoAddRequest): ResponseEntity<Response<TodoResponse>> =
        R.ok(todoService.addTodo(request))

    @PostMapping("/delete")
    @PreAuthorize("@accessCheckService.ownsTodoAccess(authentication.principal.id, #request.id)")
    fun deleteTodo(@Valid @RequestBody request: TodoDeleteRequest): ResponseEntity<Response<Unit>> {
        todoService.deleteTodo(request)
        return R.ok()
    }

    @PostMapping("/update")
    @PreAuthorize("@accessCheckService.ownsTodoAccess(authentication.principal.id, #request.id)")
    fun updateTodo(@Valid @RequestBody request: TodoUpdateRequest): ResponseEntity<Response<TodoResponse>> =
        R.ok(todoService.updateTodo(request))

    @PostMapping("/toggle")
    @PreAuthorize("@accessCheckService.ownsTodoAccess(authentication.principal.id, #request.id)")
    fun toggleTodo(@Valid @RequestBody request: TodoToggleRequest): ResponseEntity<Response<TodoResponse>> =
        R.ok(todoService.toggleTodo(request))

    @PostMapping("/move")
    fun moveTodo(@Valid @RequestBody request: TodoMoveRequest): ResponseEntity<Response<TodoResponse>> =
        R.ok(todoService.moveTodo(request))
}
