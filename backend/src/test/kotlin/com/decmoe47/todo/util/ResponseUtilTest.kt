package com.decmoe47.todo.util

import com.decmoe47.todo.constant.enums.ErrorCode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse

class ResponseUtilTest : FunSpec({
    test("writeErrMsg writes json response with error code") {
        val response = MockHttpServletResponse()

        response.writeErrMsg(ErrorCode.UNAUTHORIZED)

        response.contentType shouldBe MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
        response.characterEncoding shouldBe "UTF-8"
        response.contentAsString shouldContain "\"code\":${ErrorCode.UNAUTHORIZED.code}"
    }
})
