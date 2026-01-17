package com.decmoe47.todo.model.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class TodoAddRequest(
    @field:NotBlank
    val content: String,

    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val dueDate: LocalDateTime?,

    @field:NotNull
    @field:Min(1)
    val belongedListId: Long,
)
