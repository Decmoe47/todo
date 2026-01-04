package com.decmoe47.todo.repository

import com.decmoe47.todo.model.entity.User

interface UserRepository {
    fun first(id: Long): User?

    fun firstByEmail(email: String): User?

    fun selectByEmail(email: String): List<User>

    fun selectByName(username: String): List<User>

    fun save(user: User): User

    fun update(user: User): User
}
