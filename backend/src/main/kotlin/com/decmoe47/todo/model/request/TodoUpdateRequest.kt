package com.decmoe47.todo.model.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kotlinx.datetime.LocalDateTime

data class TodoUpdateRequest(
    @field:NotNull
    val id: Long,

    @field:NotBlank
    val content: String,

    @field:NotNull
    val done: Boolean,

    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val dueDate: LocalDateTime?,

    val description: String?,
)
