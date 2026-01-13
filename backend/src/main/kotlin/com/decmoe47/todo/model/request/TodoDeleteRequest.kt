package com.decmoe47.todo.model.request

import jakarta.validation.constraints.NotNull

data class TodoDeleteRequest(
    @field:NotNull
    val id: Long,

    @field:NotNull
    val softDeleted: Boolean,
)