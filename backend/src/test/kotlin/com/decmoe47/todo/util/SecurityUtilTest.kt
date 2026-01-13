package com.decmoe47.todo.util

import com.decmoe47.todo.exception.AuthenticationException
import com.decmoe47.todo.model.dto.SecurityUser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class SecurityUtilTest : FunSpec({
    afterTest {
        SecurityContextHolder.clearContext()
    }

    test("getCurrentUserId returns id for SecurityUser principal") {
        val principal = SecurityUser(id = 9, email = "user@test.com")
        val authentication = UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
        SecurityContextHolder.getContext().authentication = authentication

        SecurityUtil.getCurrentUserId() shouldBe 9L
    }

    test("getCurrentUserId throws when authentication principal is unknown") {
        val authentication = UsernamePasswordAuthenticationToken("invalid", null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication

        shouldThrow<AuthenticationException> {
            SecurityUtil.getCurrentUserId()
        }
    }
})
