package com.decmoe47.todo.model.response

import com.decmoe47.todo.constant.enums.ErrorCode
import kotlinx.serialization.Serializable

@Serializable
data class R<out T>(
    val code: Int = 0,
    val message: String? = "",
    val data: T? = null
) : java.io.Serializable {
    companion object {
        fun ok(): R<Unit> = R()

        fun <T> ok(data: T): R<T> = R(data = data)

        fun error(errCode: ErrorCode): R<Unit> = R(errCode.code, errCode.message)

        fun <T> error(errCode: ErrorCode, data: T): R<T> = R(errCode.code, errCode.message, data)
    }
}