package com.decmoe47.todo.model.dto

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class SecurityUser(
    val id: Long,
    val email: String,
    private val password: String? = null,
) : UserDetails {
    override fun getAuthorities() = listOf(SimpleGrantedAuthority("ROLE_USER"))

    override fun getPassword() = password

    override fun getUsername() = email
}

