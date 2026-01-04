package com.decmoe47.todo.model.request

import jakarta.validation.constraints.NotNull

data class TodoToggleRequest(
    @field:NotNull
    val id: Long,
)
