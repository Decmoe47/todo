package com.decmoe47.todo.service

import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.entity.User
import com.decmoe47.todo.model.request.UserLoginRequest
import com.decmoe47.todo.model.request.UserRegisterRequest
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.repository.UserRepository
import com.decmoe47.todo.service.impl.AuthServiceImpl
import com.decmoe47.todo.util.toSecurityUser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class AuthServiceImplTest : FunSpec({
    lateinit var mailService: MailService
    lateinit var tokenService: TokenService
    lateinit var verificationCodeService: VerificationCodeService
    lateinit var authenticationManager: AuthenticationManager
    lateinit var userRepository: UserRepository
    lateinit var todoListRepository: TodoListRepository
    lateinit var service: AuthServiceImpl

    beforeTest {
        mailService = mockk()
        tokenService = mockk()
        verificationCodeService = mockk()
        authenticationManager = mockk()
        userRepository = mockk()
        todoListRepository = mockk()

        service = AuthServiceImpl(
            mailService = mailService,
            tokenService = tokenService,
            verificationCodeService = verificationCodeService,
            authenticationManager = authenticationManager,
            userRepository = userRepository,
            todoListRepository = todoListRepository
        )
    }

    afterTest {
        SecurityContextHolder.clearContext()
    }

    test("login throws USERNAME_OR_PASSWORD_INCORRECT on bad credentials") {
        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("bad credentials")

        val error = shouldThrow<ErrorResponseException> {
            service.login(UserLoginRequest(email = "user@test.com", password = "bad"))
        }

        error.errorCode shouldBe ErrorCode.USERNAME_OR_PASSWORD_INCORRECT
    }

    test("login throws USERNAME_OR_PASSWORD_INCORRECT when principal is not user") {
        val authentication = UsernamePasswordAuthenticationToken("bad", null, emptyList())
        every { authenticationManager.authenticate(any()) } returns authentication

        val error = shouldThrow<ErrorResponseException> {
            service.login(UserLoginRequest(email = "user@test.com", password = "pw"))
        }

        error.errorCode shouldBe ErrorCode.USERNAME_OR_PASSWORD_INCORRECT
    }

    test("login returns user response and saves user") {
        val user = User(id = 5, email = "user@test.com", password = "pw", name = "name")
        val principal = user.toSecurityUser()
        val authentication = UsernamePasswordAuthenticationToken(principal, null, emptyList())
        val tokens = AuthenticationTokensResponse(accessToken = "access", refreshToken = "refresh")

        every { authenticationManager.authenticate(any()) } returns authentication
        every { userRepository.first(5L) } returns user
        every { userRepository.update(any()) } returns user
        every { tokenService.generate(authentication) } returns tokens

        val result = service.login(UserLoginRequest(email = "user@test.com", password = "pw"))

        result.id shouldBe 5L
        result.tokens?.accessToken shouldBe "access"
    }

    test("register throws USER_ALREADY_EXISTS when email is taken") {
        val existing = User(id = 1, email = "user@test.com", password = "pw", name = "name")
        every { userRepository.firstByEmail("user@test.com") } returns existing

        val error = shouldThrow<ErrorResponseException> {
            service.register(
                UserRegisterRequest(
                    email = "user@test.com",
                    password = "pw",
                    name = "name",
                    verificationCode = "1234"
                )
            )
        }

        error.errorCode shouldBe ErrorCode.USER_ALREADY_EXISTS
    }

    test("sendVerificationCode throws when mail service fails") {
        every { verificationCodeService.createCode("user@test.com") } returns "1234"
        every { mailService.send(any(), any(), any()) } returns false

        val error = shouldThrow<ErrorResponseException> {
            service.sendVerificationCode("user@test.com")
        }

        error.errorCode shouldBe ErrorCode.VERIFICATION_CODE_SEND_FAILED
    }

    test("sendVerificationCode succeeds when mail service returns true") {
        every { verificationCodeService.createCode("user@test.com") } returns "1234"
        every { mailService.send(any(), any(), any()) } returns true

        service.sendVerificationCode("user@test.com")

        verify { mailService.send(listOf("user@test.com"), any(), any()) }
    }

    test("refreshAccessToken throws when token is invalid") {
        every { tokenService.isValid("bad") } returns false

        val error = shouldThrow<ErrorResponseException> {
            service.refreshAccessToken("bad")
        }

        error.errorCode shouldBe ErrorCode.REFRESH_TOKEN_EXPIRED
    }

    test("refreshAccessToken returns new tokens when valid") {
        val tokens = AuthenticationTokensResponse(accessToken = "access", refreshToken = "refresh")
        every { tokenService.isValid("good") } returns true
        every { tokenService.refresh("good") } returns tokens

        service.refreshAccessToken("good") shouldBe tokens
    }

    test("logout invalidates token and clears context") {
        every { tokenService.invalidate("token") } just runs
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("user", null, emptyList())

        service.logout("token")

        SecurityContextHolder.getContext().authentication shouldBe null
        verify { tokenService.invalidate("token") }
    }

    test("register saves new user and creates inbox list") {
        every { userRepository.firstByEmail("new@test.com") } returns null
        every { verificationCodeService.checkCode("1234", "new@test.com") } just runs
        every { userRepository.save(any()) } answers { firstArg() }
        every { todoListRepository.save(any()) } answers { firstArg() }

        val result = service.register(
            UserRegisterRequest(
                email = "new@test.com",
                password = "pw",
                name = "name",
                verificationCode = "1234"
            )
        )

        result.email shouldBe "new@test.com"
        verify(exactly = 1) { todoListRepository.save(any()) }
    }
})
