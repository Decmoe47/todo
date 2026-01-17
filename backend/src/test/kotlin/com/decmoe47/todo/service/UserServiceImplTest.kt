package com.decmoe47.todo.service

import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.dto.SecurityUser
import com.decmoe47.todo.model.entity.User
import com.decmoe47.todo.model.request.UserSearchRequest
import com.decmoe47.todo.model.request.UserUpdateRequest
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import com.decmoe47.todo.repository.UserRepository
import com.decmoe47.todo.service.impl.UserServiceImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
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

    test("getUser returns user response when found") {
        val user = User(id = 10, email = "user@test.com", password = "pw", name = "name")
        every { userRepository.first(10L) } returns user

        val result = service.getUser(10L)

        result.id shouldBe 10L
        result.email shouldBe "user@test.com"
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

    test("searchUser returns users by email") {
        val user = User(id = 5, email = "mail@test.com", password = "pw", name = "name")
        every { userRepository.selectByEmail("mail@test.com") } returns listOf(user)

        val result = service.searchUser(UserSearchRequest(id = null, name = null, email = "mail@test.com"))

        result.first().email shouldBe "mail@test.com"
    }

    test("searchUser returns user by id") {
        val user = User(id = 2, email = "id@test.com", password = "pw", name = "name")
        every { userRepository.first(2L) } returns user

        val result = service.searchUser(UserSearchRequest(id = 2, name = null, email = null))

        result.first().id shouldBe 2L
    }

    test("searchUser returns users by name") {
        val user = User(id = 3, email = "name@test.com", password = "pw", name = "Name")
        every { userRepository.selectByName("Name") } returns listOf(user)

        val result = service.searchUser(UserSearchRequest(id = null, name = "Name", email = null))

        result.first().id shouldBe 3L
    }

    test("searchUser throws USER_NOT_FOUND when name lookup returns empty") {
        every { userRepository.selectByName("missing") } returns emptyList()

        val error = shouldThrow<ErrorResponseException> {
            service.searchUser(UserSearchRequest(id = null, name = "missing", email = null))
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

    test("updateUser keeps values when name and email are blank") {
        val user = User(id = 1, email = "old@test.com", password = "pw", name = "old")
        val request = UserUpdateRequest(
            id = 1,
            name = " ",
            email = "",
            verificationCode = "1234"
        )
        every { userRepository.first(1L) } returns user
        every { userRepository.update(user) } returns user

        val result = service.updateUser(1L, request)

        result.name shouldBe "old"
        result.email shouldBe "old@test.com"
        verify(exactly = 0) { verificationCodeService.checkCode(any(), any()) }
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

    test("getUserByToken throws USER_NOT_FOUND when user record is missing") {
        val principal = SecurityUser(id = 2, email = "user@test.com")
        val authentication = UsernamePasswordAuthenticationToken(principal, null, emptyList())

        every { tokenService.isValid("good") } returns true
        every { tokenService.parse("good") } returns authentication
        every { userRepository.first(2L) } returns null

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

    test("loadUserByUsername returns security user when found") {
        val user = User(id = 4, email = "u@test.com", password = "pw", name = "name")
        every { userRepository.firstByEmail("u@test.com") } returns user

        val result = service.loadUserByUsername("u@test.com") as SecurityUser

        result.id shouldBe 4L
        result.password shouldBe "pw"
    }

    test("getUserByToken returns user response when token is valid") {
        val principal = SecurityUser(id = 2, email = "user@test.com")
        val user = User(id = 2, email = "user@test.com", password = "pw", name = "name")
        val authentication = UsernamePasswordAuthenticationToken(principal, null, emptyList())

        every { tokenService.isValid("good") } returns true
        every { tokenService.parse("good") } returns authentication
        every { userRepository.first(2L) } returns user
        every { tokenService.generate(any<SecurityUser>()) } returns AuthenticationTokensResponse("access", "refresh")

        val result = service.getUserByToken("good")

        result.id shouldBe 2L
        result.email shouldBe "user@test.com"
    }
})
