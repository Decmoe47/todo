package com.decmoe47.todo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class Response<out T>(
    val code: Int = 0,
    val message: String? = "",
    val data: T? = null
) : java.io.Serializable {
}