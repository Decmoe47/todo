package com.decmoe47.todo.service.impl

import com.decmoe47.todo.annotation.ReadOnlyTransactionalService
import com.decmoe47.todo.constant.MailTemplate
import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.dto.SecurityUser
import com.decmoe47.todo.model.entity.AuditableEntity
import com.decmoe47.todo.model.entity.TodoList
import com.decmoe47.todo.model.entity.User
import com.decmoe47.todo.model.mapper.toUser
import com.decmoe47.todo.model.mapper.toUserResponse
import com.decmoe47.todo.model.request.UserLoginRequest
import com.decmoe47.todo.model.request.UserRegisterRequest
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import com.decmoe47.todo.model.response.UserResponse
import com.decmoe47.todo.repository.TodoListRepository
import com.decmoe47.todo.repository.UserRepository
import com.decmoe47.todo.service.AuthService
import com.decmoe47.todo.service.MailService
import com.decmoe47.todo.service.TokenService
import com.decmoe47.todo.service.VerificationCodeService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@ReadOnlyTransactionalService
class AuthServiceImpl(
    private val mailService: MailService,
    private val tokenService: TokenService,
    private val verificationCodeService: VerificationCodeService,
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val todoListRepository: TodoListRepository,
) : AuthService {
    @Transactional
    override fun login(request: UserLoginRequest): UserResponse {
        val authentication: Authentication = authenticate(request.email, request.password)
        val principal = authentication.principal as? SecurityUser
            ?: throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)
        val user = userRepository.first(principal.id) ?: throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)
        val authenticationTokensResponse: AuthenticationTokensResponse = tokenService.generate(authentication)

        return user.toUserResponse(authenticationTokensResponse)
    }

    override fun logout(token: String) {
        tokenService.invalidate(token)
        SecurityContextHolder.clearContext()
    }

    @Transactional
    override fun register(request: UserRegisterRequest): UserResponse {
        if (userRepository.firstByEmail(request.email) != null)
            throw ErrorResponseException(ErrorCode.USER_ALREADY_EXISTS)

        verificationCodeService.checkCode(request.verificationCode, request.email)

        val tmpUser: User = request.toUser()
        val user = saveNewUser(tmpUser)

        val todoList = TodoList(name = "Inbox", inbox = true, auditable = AuditableEntity(createdBy = user.id))
        todoListRepository.save(todoList)

        return user.toUserResponse()
    }

    override fun sendVerificationCode(email: String) {
        val code = verificationCodeService.createCode(email)
        val sent = mailService.send(
            listOf(email),
            MailTemplate.VERIFICATION_CODE_SUBJECT,
            MailTemplate.VERIFICATION_CODE_BODY.replace("{code}", code)
        )
        if (!sent)
            throw ErrorResponseException(ErrorCode.VERIFICATION_CODE_SEND_FAILED)
    }

    override fun refreshAccessToken(refreshToken: String): AuthenticationTokensResponse {
        if (!tokenService.isValid(refreshToken))
            throw ErrorResponseException(ErrorCode.REFRESH_TOKEN_EXPIRED)
        return tokenService.refresh(refreshToken)
    }

    private fun authenticate(email: String, password: String): Authentication {
        try {
            val token = UsernamePasswordAuthenticationToken(email, password)
            val authentication = authenticationManager.authenticate(token)

            val principal = authentication.principal as? SecurityUser
                ?: throw ErrorResponseException(ErrorCode.USERNAME_OR_PASSWORD_INCORRECT)
            val user = userRepository.first(principal.id)
                ?: throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)
            userRepository.update(user.copy(lastLoginTime = LocalDateTime.now()))

            SecurityContextHolder.getContext().authentication = authentication
            return authentication
        } catch (e: BadCredentialsException) {
            throw ErrorResponseException(ErrorCode.USERNAME_OR_PASSWORD_INCORRECT, e)
        }
    }

    private fun saveNewUser(user: User): User {
        val bCryptPasswordEncoder = BCryptPasswordEncoder()
        val encodedPassword = bCryptPasswordEncoder.encode(user.password)
            ?: throw ErrorResponseException(ErrorCode.INTERNAL_SERVER_ERROR, "密码加密失败！")
        val user = user.copy(password = encodedPassword)
        return userRepository.save(user)
    }
}
