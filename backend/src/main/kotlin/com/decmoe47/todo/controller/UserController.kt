package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.UserSearchRequest
import com.decmoe47.todo.model.request.UserUpdateRequest
import com.decmoe47.todo.model.response.Response
import com.decmoe47.todo.model.response.UserResponse
import com.decmoe47.todo.service.UserService
import com.decmoe47.todo.util.R
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {
    @Operation(summary = "获取用户")
    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Long): ResponseEntity<Response<UserResponse?>> =
        R.ok(userService.getUser(userId))

    @GetMapping("/by-token")
    fun getUserByToken(@RequestParam token: String): ResponseEntity<Response<UserResponse>> =
        R.ok(userService.getUserByToken(token))

    @GetMapping("/search")
    fun searchUser(@RequestParam request: UserSearchRequest): ResponseEntity<Response<List<UserResponse>>> =
        R.ok(userService.searchUser(request))

    @PostMapping("/{userId}/update")
    fun updateUser(
        @PathVariable userId: Long,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<Response<UserResponse>> =
        R.ok(userService.updateUser(userId, request))
}
