package com.decmoe47.todo.model.request

import jakarta.validation.constraints.NotNull

data class TodoListDeleteRequest(
    @field:NotNull
    val id: Long,
)
