package com.decmoe47.todo.controller

import com.decmoe47.todo.model.request.UserSearchRequest
import com.decmoe47.todo.model.request.UserUpdateRequest
import com.decmoe47.todo.model.response.UserResponse
import com.decmoe47.todo.service.UserService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UserControllerTest : FunSpec({
    val userService = mockk<UserService>()
    val controller = UserController(userService)
    val response = UserResponse(id = 1, email = "user@test.com", name = "User")

    beforeTest {
        clearMocks(userService)
    }

    test("getUser returns response") {
        every { userService.getUser(1) } returns response

        controller.getUser(1).body?.data shouldBe response

        verify { userService.getUser(1) }
    }

    test("getUserByToken returns response") {
        every { userService.getUserByToken("token") } returns response

        controller.getUserByToken("token").body?.data shouldBe response

        verify { userService.getUserByToken("token") }
    }

    test("searchUser returns list") {
        val request = UserSearchRequest(id = null, name = "User", email = null)
        every { userService.searchUser(request) } returns listOf(response)

        controller.searchUser(request).body?.data shouldBe listOf(response)

        verify { userService.searchUser(request) }
    }

    test("updateUser returns response") {
        val request = UserUpdateRequest(
            id = 1,
            name = "User",
            email = "user@test.com",
            verificationCode = "1234"
        )
        every { userService.updateUser(1, request) } returns response

        controller.updateUser(1, request).body?.data shouldBe response

        verify { userService.updateUser(1, request) }
    }
})
