package com.decmoe47.todo.handler

import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.dto.SecurityUser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mock.http.MockHttpInputMessage
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.context.SecurityContextHolder

class GlobalExceptionHandlerTest : FunSpec({
    val handler = GlobalExceptionHandler()

    afterTest {
        SecurityContextHolder.clearContext()
    }

    test("handleException delegates to error response handler") {
        val result = handler.handleException(mockk(), ErrorResponseException(ErrorCode.TODO_NOT_FOUND))

        result.body?.code shouldBe ErrorCode.TODO_NOT_FOUND.code
    }

    test("handleException returns access denied for authorization error") {
        val request = mockk<HttpServletRequest>()
        every { request.requestURI } returns "/api/test"

        val principal = SecurityUser(id = 1, email = "user@test.com")
        val authentication = UsernamePasswordAuthenticationToken(principal, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication

        val exception = AuthorizationDeniedException("denied", AuthorizationDecision(false))

        val result = handler.handleException(request, exception)

        result.body?.code shouldBe ErrorCode.ACCESS_DENIED.code
    }

    test("handleException returns internal server error on unknown exception") {
        val result = handler.handleException(mockk(), RuntimeException("boom"))

        result.body?.code shouldBe ErrorCode.INTERNAL_SERVER_ERROR.code
    }

    test("handleErrorResponseException includes data when provided") {
        val data = mapOf("field" to "value")
        val result =
            handler.handleErrorResponseException(ErrorResponseException(ErrorCode.INVALID_REQUEST_PARAMS, data))

        result.body?.code shouldBe ErrorCode.INVALID_REQUEST_PARAMS.code
        result.body?.data shouldBe data
    }

    test("handleErrorResponseException returns error without data") {
        val result = handler.handleErrorResponseException(ErrorResponseException(ErrorCode.TODO_LIST_NOT_FOUND))

        result.body?.code shouldBe ErrorCode.TODO_LIST_NOT_FOUND.code
        result.body?.data shouldBe null
    }

    test("handleHttpMessageNotReadable returns field error for null primitive") {
        val mapper: ObjectMapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        data class Payload(val age: Int)

        val mismatched = shouldThrow<MismatchedInputException> {
            mapper.readValue("""{"age":null}""", Payload::class.java)
        }
        val httpEx = HttpMessageNotReadableException(
            "bad body",
            mismatched,
            MockHttpInputMessage("{}".toByteArray())
        )

        val response = handler.handleHttpMessageNotReadable(httpEx)

        response.statusCode.value() shouldBe 400
        response.body?.get("message").toString().contains("缺失或不能为 null") shouldBe true
    }

    test("handleHttpMessageNotReadable returns type error for mismatched type") {
        val mapper: ObjectMapper = jacksonObjectMapper()

        data class Payload(val age: Int)

        val mismatched = shouldThrow<MismatchedInputException> {
            mapper.readValue("""{"age":"bad"}""", Payload::class.java)
        }
        val httpEx = HttpMessageNotReadableException(
            "bad body",
            mismatched,
            MockHttpInputMessage("{}".toByteArray())
        )

        val response = handler.handleHttpMessageNotReadable(httpEx)

        response.statusCode.value() shouldBe 400
        response.body?.get("message").toString().contains("类型错误") shouldBe true
    }

    test("handleHttpMessageNotReadable returns generic message when cause not mismatched") {
        val httpEx = HttpMessageNotReadableException(
            "bad body",
            RuntimeException("bad"),
            MockHttpInputMessage("{}".toByteArray())
        )

        val response = handler.handleHttpMessageNotReadable(httpEx)

        response.statusCode.value() shouldBe 400
        response.body?.get("message") shouldBe "请求体格式错误"
    }
})
