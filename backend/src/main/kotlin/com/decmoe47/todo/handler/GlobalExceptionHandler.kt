package com.decmoe47.todo.handler

import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.response.Response
import com.decmoe47.todo.util.R
import com.decmoe47.todo.util.SecurityUtil
import com.decmoe47.todo.util.rootCause
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = KotlinLogging.logger {}

@RestControllerAdvice(basePackages = ["com.decmoe47.todo.controller"])
class GlobalExceptionHandler {
    @ExceptionHandler(Throwable::class)
    fun handleException(request: HttpServletRequest, e: Throwable): ResponseEntity<out Response<Any>> {
        return when (val rootCause = e.rootCause()) {
            is ErrorResponseException -> handleErrorResponseException(rootCause)
            is AuthorizationDeniedException -> handleAuthorizationDeniedException(request, rootCause)
            else -> {
                log.error(rootCause) { "${rootCause.message}" }
                R.error(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @ExceptionHandler(ErrorResponseException::class)
    fun handleErrorResponseException(e: ErrorResponseException): ResponseEntity<out Response<Any>> {
        log.error(e.rootCause()) { "${e.rootCause().message}" }
        return if (e.data != null) R.error(e.errorCode, e.data)
        else R.error(e.errorCode)
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDeniedException(
        request: HttpServletRequest,
        e: AuthorizationDeniedException
    ): ResponseEntity<out Response<Any>> {
        log.error(e) { "Failed to authorize for userId: ${SecurityUtil.getCurrentUserId()} and api: ${request.requestURI}" }
        return R.error(ErrorCode.ACCESS_DENIED)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        request: HttpServletRequest,
        e: MethodArgumentNotValidException
    ): ResponseEntity<out Response<Any>> {
        log.error(e) { "Failed to validate the request for userId: ${SecurityUtil.getCurrentUserId()} and api: ${request.requestURI}" }
        return R.error(ErrorCode.INVALID_REQUEST_PARAMS)
    }

    /**
     * 对于kotlin非空基本类型编译后成为java基本类型的字段校验，jackson会直接返回默认值，导致逃过了 `@NotNull` 校验。
     * 开启jackson的 `FAIL_ON_NULL_FOR_PRIMITIVES` 后则会抛出异常，然后在全局异常处理中伪装成和spring校验失败时一样的response，
     * 以此来解决该问题。
     *
     * @see tools.jackson.databind.deser.std.StdDeserializer._verifyNullForPrimitive
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<Map<String, Any>> {
        val cause = ex.cause
        if (cause is MismatchedInputException) {
            val fieldName = cause.path.joinToString(".") { it.fieldName ?: "" }

            val errorMsg = if (cause.targetType?.isPrimitive == true
                && cause.message?.contains("FAIL_ON_NULL_FOR_PRIMITIVES") == true
            ) "字段 [$fieldName] 缺失或不能为 null"
            else "字段 [$fieldName] 类型错误"

            return ResponseEntity.badRequest().body(
                mapOf(
                    "code" to 400,
                    "field" to fieldName,
                    "message" to errorMsg
                )
            )
        }
        return ResponseEntity.badRequest().body(mapOf("message" to "请求体格式错误"))
    }
}