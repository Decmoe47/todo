package com.decmoe47.todo.model.response

import kotlinx.datetime.LocalDateTime

data class TodoResponse(
    val id: Long,
    val content: String,
    val dueDate: LocalDateTime?,
    val done: Boolean,
    val description: String?,
    val belongedListId: Long,

    val createdBy: Long,
    val createdAt: LocalDateTime,
    val updatedBy: Long?,
    val updatedAt: LocalDateTime?,
)
