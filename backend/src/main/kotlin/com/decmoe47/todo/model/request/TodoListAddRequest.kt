package com.decmoe47.todo.model.request

import jakarta.validation.constraints.NotBlank

data class TodoListAddRequest(
    @field:NotBlank
    val name: String,
)
