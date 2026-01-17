package com.decmoe47.todo.model.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class TodoToggleRequest(
    @field:NotNull
    @field:Min(1)
    val id: Long,
)
