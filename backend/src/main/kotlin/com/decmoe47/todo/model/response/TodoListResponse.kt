package com.decmoe47.todo.model.response

import java.time.LocalDateTime

data class TodoListResponse(
    val id: Long,
    val name: String,
    val inbox: Boolean,

    val createdBy: Long,
    val createdAt: LocalDateTime,
    val updatedBy: Long?,
    val updatedAt: LocalDateTime?,
)
