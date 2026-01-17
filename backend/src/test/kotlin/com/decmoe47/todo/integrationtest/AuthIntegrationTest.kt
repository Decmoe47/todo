package com.decmoe47.todo.integrationtest

import com.decmoe47.todo.model.request.RefreshTokenRequest
import com.decmoe47.todo.model.request.UserLoginRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("integration")
class AuthIntegrationTest : IntegrationTestBase() {

    @Test
    fun `refresh token returns new access token`() {
        val (accessToken, _) = registerAndLogin("refresh-token@test.com")
        val loginResponse = post(
            "/api/auth/login",
            UserLoginRequest(email = "refresh-token@test.com", password = "pw123456"),
            userResponseType
        )
        val refreshToken = requireNotNull(loginResponse.data?.tokens?.refreshToken)

        val refreshed = post(
            "/api/auth/refresh-token",
            RefreshTokenRequest(refreshToken = refreshToken),
            tokenResponseType
        )
        val newAccessToken = requireNotNull(refreshed.data?.accessToken)
        val newRefreshToken = requireNotNull(refreshed.data.refreshToken)
        Assertions.assertThat(newAccessToken).isNotBlank
        Assertions.assertThat(newRefreshToken).isEqualTo(refreshToken)
        Assertions.assertThat(newAccessToken).isNotEqualTo(accessToken)
    }
}
