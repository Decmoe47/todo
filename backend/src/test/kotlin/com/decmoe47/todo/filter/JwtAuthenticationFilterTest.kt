package com.decmoe47.todo.filter

import com.decmoe47.todo.constant.JwtConstants
import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.service.TokenService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class JwtAuthenticationFilterTest : FunSpec({
    val tokenService = mockk<TokenService>()
    val filter = JwtAuthenticationFilter(tokenService)

    afterTest {
        SecurityContextHolder.clearContext()
    }

    test("whitelisted uri passes through without token checks") {
        val request = MockHttpServletRequest("GET", "/api/auth/login")
        val response = MockHttpServletResponse()
        val chain = mockk<jakarta.servlet.FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { chain.doFilter(request, response) }
        verify(exactly = 0) { tokenService.isValid(any()) }
    }

    test("missing authorization header returns unauthorized error") {
        val request = MockHttpServletRequest("GET", "/api/todos")
        val response = MockHttpServletResponse()
        val chain = mockk<jakarta.servlet.FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        response.contentAsString shouldContain "\"code\":${ErrorCode.UNAUTHORIZED.code}"
        verify(exactly = 0) { chain.doFilter(any(), any()) }
    }

    test("authorization header without bearer prefix returns unauthorized error") {
        val request = MockHttpServletRequest("GET", "/api/todos")
        request.addHeader(HttpHeaders.AUTHORIZATION, "Token bad")
        val response = MockHttpServletResponse()
        val chain = mockk<jakarta.servlet.FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        response.contentAsString shouldContain "\"code\":${ErrorCode.UNAUTHORIZED.code}"
        verify(exactly = 0) { chain.doFilter(any(), any()) }
    }

    test("invalid token returns access token expired error") {
        val request = MockHttpServletRequest("GET", "/api/todos")
        request.addHeader(HttpHeaders.AUTHORIZATION, "${JwtConstants.BEARER_PREFIX}bad")
        val response = MockHttpServletResponse()
        val chain = mockk<jakarta.servlet.FilterChain>(relaxed = true)

        every { tokenService.isValid("bad") } returns false

        filter.doFilter(request, response, chain)

        response.contentAsString shouldContain "\"code\":${ErrorCode.ACCESS_TOKEN_EXPIRED.code}"
        verify(exactly = 0) { chain.doFilter(any(), any()) }
    }

    test("valid token sets security context and continues") {
        val request = MockHttpServletRequest("GET", "/api/todos")
        request.addHeader(HttpHeaders.AUTHORIZATION, "${JwtConstants.BEARER_PREFIX}good")
        val response = MockHttpServletResponse()
        val chain = mockk<jakarta.servlet.FilterChain>(relaxed = true)
        val authentication = UsernamePasswordAuthenticationToken("principal", null, emptyList())

        every { tokenService.isValid("good") } returns true
        every { tokenService.parse("good") } returns authentication

        filter.doFilter(request, response, chain)

        SecurityContextHolder.getContext().authentication shouldBe authentication
        verify(exactly = 1) { chain.doFilter(request, response) }
    }
})
