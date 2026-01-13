package com.decmoe47.todo.util

import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.model.response.Response
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.io.IOException
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger {}

fun HttpServletResponse.writeErrMsg(errCode: ErrorCode) {
    this.contentType = MediaType.APPLICATION_JSON_VALUE
    this.characterEncoding = StandardCharsets.UTF_8.name()

    try {
        this.writer.use { writer ->
            val jsonResponse = Json.encodeToString(
                Response.serializer(
                    serializer<ResponseEntity<Response<Unit>>>()
                ), Response(code = errCode.code)
            )
            writer.print(jsonResponse)
            writer.flush()
        }
    } catch (e: IOException) {
        log.error(e) { "Failed to write error message to response" }
    }
}

object R {
    @JvmStatic
    fun ok(): ResponseEntity<Response<Unit>> = ResponseEntity.ok(Response())

    @JvmStatic
    fun <T> ok(data: T): ResponseEntity<Response<T>> = ResponseEntity.ok(Response(data = data))

    @JvmStatic
    fun error(errCode: ErrorCode): ResponseEntity<Response<Unit>> =
        ResponseEntity.status(errCode.httpStatus).body(Response(errCode.code, errCode.message))

    @JvmStatic
    fun <T> error(errCode: ErrorCode, data: T): ResponseEntity<Response<T>> =
        ResponseEntity.status(errCode.httpStatus).body(Response(errCode.code, errCode.message, data))
}

