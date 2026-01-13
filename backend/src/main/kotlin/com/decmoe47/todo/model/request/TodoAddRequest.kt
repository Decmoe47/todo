package com.decmoe47.todo.model.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import kotlinx.datetime.LocalDateTime

data class TodoAddRequest(
    @field:NotBlank
    val content: String,

    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val dueDate: LocalDateTime?,

    @field:NotBlank
    val belongedListId: String,
)
