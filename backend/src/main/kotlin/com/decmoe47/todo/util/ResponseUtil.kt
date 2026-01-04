package com.decmoe47.todo.util

import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.model.response.R
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.http.MediaType
import java.io.IOException
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger {}

fun HttpServletResponse.writeErrMsg(errCode: ErrorCode) {
    this.contentType = MediaType.APPLICATION_JSON_VALUE
    this.characterEncoding = StandardCharsets.UTF_8.name()

    try {
        this.writer.use { writer ->
            val jsonResponse = Json.encodeToString(R.serializer(serializer<Unit>()), R.error(errCode))
            writer.print(jsonResponse)
            writer.flush()
        }
    } catch (e: IOException) {
        log.error(e) { "Failed to write error message to response" }
    }
}
