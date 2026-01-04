package com.decmoe47.todo.service

import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.dto.SecurityUser
import com.decmoe47.todo.model.entity.User
import com.decmoe47.todo.model.request.UserSearchRequest
import com.decmoe47.todo.model.request.UserUpdateRequest
import com.decmoe47.todo.repository.UserRepository
import com.decmoe47.todo.service.impl.UserServiceImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class UserServiceImplTest : FunSpec({
    lateinit var userRepository: UserRepository
    lateinit var verificationCodeService: VerificationCodeService
    lateinit var tokenService: TokenService
    lateinit var service: UserServiceImpl

    beforeTest {
        userRepository = mockk()
        verificationCodeService = mockk()
        tokenService = mockk()
        service = UserServiceImpl(userRepository, verificationCodeService, tokenService)
    }

    test("getUser throws USER_NOT_FOUND when repository returns null") {
        every { userRepository.first(10L) } returns null

        val error = shouldThrow<ErrorResponseException> {
            service.getUser(10L)
        }

        error.errorCode shouldBe ErrorCode.USER_NOT_FOUND
    }

    test("searchUser throws NO_QUERY_PARAM_PROVIDED when all params are null") {
        val error = shouldThrow<ErrorResponseException> {
            service.searchUser(UserSearchRequest(id = null, name = null, email = null))
        }

        error.errorCode shouldBe ErrorCode.NO_QUERY_PARAM_PROVIDED
    }

    test("searchUser throws USER_NOT_FOUND when email lookup returns empty") {
        every { userRepository.selectByEmail("user@test.com") } returns emptyList()

        val error = shouldThrow<ErrorResponseException> {
            service.searchUser(UserSearchRequest(id = null, name = null, email = "user@test.com"))
        }

        error.errorCode shouldBe ErrorCode.USER_NOT_FOUND
    }

    test("updateUser updates name and email with verification check") {
        val user = User(id = 1, email = "old@test.com", password = "pw", name = "old")
        val updated = user.copy(email = "new@test.com", name = "new")
        val request = UserUpdateRequest(
            id = 1,
            name = "new",
            email = "new@test.com",
            verificationCode = "1234"
        )

        every { userRepository.first(1L) } returns user
        every { verificationCodeService.checkCode("1234", "new@test.com") } just runs
        every { userRepository.update(updated) } returns updated

        val result = service.updateUser(1L, request)

        result.email shouldBe "new@test.com"
        result.name shouldBe "new"
    }

    test("getUserByToken throws ACCESS_TOKEN_EXPIRED when token is invalid") {
        every { tokenService.isValid("bad") } returns false

        val error = shouldThrow<ErrorResponseException> {
            service.getUserByToken("bad")
        }

        error.errorCode shouldBe ErrorCode.ACCESS_TOKEN_EXPIRED
    }

    test("getUserByToken throws USER_NOT_FOUND when token principal is unknown") {
        every { tokenService.isValid("good") } returns true
        every { tokenService.parse("good") } returns UsernamePasswordAuthenticationToken("invalid", null, emptyList())

        val error = shouldThrow<ErrorResponseException> {
            service.getUserByToken("good")
        }

        error.errorCode shouldBe ErrorCode.USER_NOT_FOUND
    }

    test("loadUserByUsername throws USER_NOT_FOUND when email is missing") {
        every { userRepository.firstByEmail("missing@test.com") } returns null

        val error = shouldThrow<ErrorResponseException> {
            service.loadUserByUsername("missing@test.com")
        }

        error.errorCode shouldBe ErrorCode.USER_NOT_FOUND
    }

    test("getUserByToken returns user response when token is valid") {
        val principal = SecurityUser(id = 2, email = "user@test.com")
        val user = User(id = 2, email = "user@test.com", password = "pw", name = "name")
        val authentication = UsernamePasswordAuthenticationToken(principal, null, emptyList())

        every { tokenService.isValid("good") } returns true
        every { tokenService.parse("good") } returns authentication
        every { userRepository.first(2L) } returns user

        val result = service.getUserByToken("good")

        result.id shouldBe 2L
        result.email shouldBe "user@test.com"
    }
})
