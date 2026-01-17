package com.decmoe47.todo.integrationtest

import com.decmoe47.todo.model.request.SendVerifyCodeRequest
import com.decmoe47.todo.model.request.UserUpdateRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("integration")
class UserIntegrationTest : IntegrationTestBase() {

    @Test
    fun `update user and token based fetch works`() {
        val (accessToken, userId) = registerAndLogin("update-user@test.com")
        val newEmail = "update-user-2@test.com"

        post(
            "/api/auth/send-verify-code",
            SendVerifyCodeRequest(newEmail),
            anyResponseType
        )
        val verificationCode = verificationCodeService.getCode(newEmail)
        val updatedUser = post(
            "/api/users/$userId/update",
            UserUpdateRequest(
                id = userId,
                name = "User Updated",
                email = newEmail,
                verificationCode = verificationCode
            ),
            userResponseType,
            accessToken
        )
        Assertions.assertThat(updatedUser.data?.email).isEqualTo(newEmail)

        val byToken = get(
            "/api/users/by-token?token=Bearer%20$accessToken",
            userResponseType,
            accessToken
        )
        Assertions.assertThat(byToken.data?.email).isEqualTo(newEmail)
    }
}
