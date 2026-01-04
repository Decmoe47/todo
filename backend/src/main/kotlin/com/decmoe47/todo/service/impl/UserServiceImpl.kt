package com.decmoe47.todo.service.impl

import com.decmoe47.todo.annotation.ReadOnlyTransactionalService
import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.exception.ErrorResponseException
import com.decmoe47.todo.model.dto.SecurityUser
import com.decmoe47.todo.model.mapper.toUserResponse
import com.decmoe47.todo.model.request.UserSearchRequest
import com.decmoe47.todo.model.request.UserUpdateRequest
import com.decmoe47.todo.model.response.UserResponse
import com.decmoe47.todo.repository.UserRepository
import com.decmoe47.todo.service.TokenService
import com.decmoe47.todo.service.UserService
import com.decmoe47.todo.service.VerificationCodeService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.transaction.annotation.Transactional

@ReadOnlyTransactionalService
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val verificationCodeService: VerificationCodeService,
    private val tokenService: TokenService,
) : UserService {
    override fun getUser(userId: Long): UserResponse {
        val user = userRepository.first(userId) ?: throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)
        return user.toUserResponse()
    }

    override fun searchUser(request: UserSearchRequest): List<UserResponse> {
        request.id?.let { return listOf(getUser(it)) }

        request.email?.let { email ->
            val users = userRepository.selectByEmail(email)
            if (users.isEmpty()) throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)
            return users.map { it.toUserResponse() }
        }

        request.name?.let { name ->
            val users = userRepository.selectByName(name)
            if (users.isEmpty()) throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)
            return users.map { it.toUserResponse() }
        }

        throw ErrorResponseException(ErrorCode.NO_QUERY_PARAM_PROVIDED)
    }

    @Transactional
    override fun updateUser(userId: Long, request: UserUpdateRequest): UserResponse {
        val user = userRepository.first(userId) ?: throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)

        var updatedUser = user
        request.name?.takeIf { it.isNotBlank() }?.let { updatedUser = updatedUser.copy(name = it) }
        request.email?.takeIf { it.isNotBlank() }?.let { email ->
            verificationCodeService.checkCode(request.verificationCode, email)
            updatedUser = updatedUser.copy(email = email)
        }

        return userRepository.update(updatedUser).toUserResponse()
    }

    override fun getUserByToken(token: String): UserResponse {
        if (!tokenService.isValid(token)) {
            throw ErrorResponseException(ErrorCode.ACCESS_TOKEN_EXPIRED)
        }

        val principal = tokenService.parse(token).principal as? SecurityUser
            ?: throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)
        val user = userRepository.first(principal.id) ?: throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)
        return user.toUserResponse()
    }

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.firstByEmail(email) ?: throw ErrorResponseException(ErrorCode.USER_NOT_FOUND)
        return SecurityUser(id = user.id, email = user.email, password = user.password)
    }
}
