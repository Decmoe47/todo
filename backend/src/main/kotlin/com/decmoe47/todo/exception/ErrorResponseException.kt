package com.decmoe47.todo.exception

import com.decmoe47.todo.constant.enums.ErrorCode

class ErrorResponseException(
    val errorCode: ErrorCode,
    val data: Any? = null,
    message: String = errorCode.message,
    cause: Throwable? = null
) : Exception(message, cause)