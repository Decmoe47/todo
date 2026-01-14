package com.decmoe47.todo.model.response

data class Response<out T>(
    val code: Int = 0,
    val message: String? = "",
    val data: T? = null
)