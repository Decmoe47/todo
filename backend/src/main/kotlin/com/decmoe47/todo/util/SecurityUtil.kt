package com.decmoe47.todo.util

import com.decmoe47.todo.exception.AuthenticationException
import com.decmoe47.todo.model.dto.SecurityUser
import com.decmoe47.todo.model.entity.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtil {
    fun getAuthentication(): Authentication? = SecurityContextHolder.getContext().authentication

    fun getCurrentUserId(): Long {
        return when (val principal = getAuthentication()?.principal) {
            is User -> principal.id
            is SecurityUser -> principal.id
            else -> throw AuthenticationException("Failed to get current user id")
        }
    }
}
