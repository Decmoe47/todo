package com.decmoe47.todo.model.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TodoMoveRequest(
    @field:NotNull
    val id: Long,

    @field:NotBlank
    val targetListId: String,
)
