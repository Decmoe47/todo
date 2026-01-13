package com.decmoe47.todo.service

import com.decmoe47.todo.model.request.UserSearchRequest
import com.decmoe47.todo.model.request.UserUpdateRequest
import com.decmoe47.todo.model.response.UserResponse
import org.springframework.security.core.userdetails.UserDetailsService

interface UserService : UserDetailsService {
    fun getUser(userId: Long): UserResponse
    fun searchUser(request: UserSearchRequest): List<UserResponse>
    fun updateUser(userId: Long, request: UserUpdateRequest): UserResponse
    fun getUserByToken(token: String): UserResponse
}
