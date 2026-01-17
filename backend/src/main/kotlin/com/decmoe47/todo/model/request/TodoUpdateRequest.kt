package com.decmoe47.todo.model.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class TodoUpdateRequest(
    @field:NotNull
    @field:Min(1)
    val id: Long,

    @field:NotBlank
    val content: String,

    @field:NotNull
    val done: Boolean,

    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val dueDate: LocalDateTime?,

    val description: String?,
)
