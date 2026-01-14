package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.TodoListAddRequest
import com.decmoe47.todo.model.request.TodoListDeleteRequest
import com.decmoe47.todo.model.request.TodoListUpdateRequest
import com.decmoe47.todo.model.response.Response
import com.decmoe47.todo.model.response.TodoListResponse
import com.decmoe47.todo.service.TodoListService
import com.decmoe47.todo.util.R
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todoLists")
class TodoListController(private val todoListService: TodoListService) {
    @GetMapping("/all")
    fun getAll(): ResponseEntity<Response<List<TodoListResponse>>> =
        R.ok(todoListService.getAll())

    @PostMapping("/add")
    fun add(@Valid @RequestBody request: TodoListAddRequest): ResponseEntity<Response<TodoListResponse>> =
        R.ok(todoListService.add(request))

    @PostMapping("/update")
    @PreAuthorize("@accessCheckService.ownsTodoListAccess(authentication.principal.id, #request.id)")
    fun update(@Valid @RequestBody request: TodoListUpdateRequest): ResponseEntity<Response<TodoListResponse>> =
        R.ok(todoListService.update(request))

    @PostMapping("/delete")
    @PreAuthorize("@accessCheckService.ownsTodoListAccess(authentication.principal.id, #request.id)")
    fun delete(@Valid @RequestBody request: TodoListDeleteRequest): ResponseEntity<Response<Unit>> {
        todoListService.delete(request)
        return R.ok()
    }
}
