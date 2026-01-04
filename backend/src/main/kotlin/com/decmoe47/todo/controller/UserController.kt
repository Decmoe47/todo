package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.UserSearchRequest
import com.decmoe47.todo.model.request.UserUpdateRequest
import com.decmoe47.todo.model.response.R
import com.decmoe47.todo.model.response.UserResponse
import com.decmoe47.todo.service.UserService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {
    @Operation(summary = "获取用户")
    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Long): R<UserResponse?> = R.ok(userService.getUser(userId))

    @GetMapping("/by-token")
    fun getUserByToken(@RequestParam token: String): R<UserResponse> = R.ok(userService.getUserByToken(token))

    @GetMapping("/search")
    fun searchUser(@RequestParam request: UserSearchRequest): R<List<UserResponse>> =
        R.ok(userService.searchUser(request))

    @PostMapping("/{userId}/update")
    fun updateUser(@PathVariable userId: Long, @Valid @RequestBody request: UserUpdateRequest): R<UserResponse> =
        R.ok(userService.updateUser(userId, request))
}
