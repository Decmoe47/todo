package com.decmoe47.todo

import com.decmoe47.todo.model.request.*
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import com.decmoe47.todo.model.response.Response
import com.decmoe47.todo.model.response.UserResponse
import com.decmoe47.todo.service.VerificationCodeService
import com.decmoe47.todo.test.TestMailConfig
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestMailConfig::class)
@Testcontainers
class IntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var verificationCodeService: VerificationCodeService

    @Test
    fun `full stack flow works`() {
        val (accessToken, _) = registerAndLogin("user@test.com")

        val todoListResponse = post(
            "/api/todoLists/add",
            TodoListAddRequest(name = "Work"),
            todoListResponseType,
            accessToken
        )
        val todoListId = requireNotNull(todoListResponse.data?.id)

        val todoResponse = post(
            "/api/todos/add",
            TodoAddRequest(content = "Write integration tests", dueDate = null, belongedListId = todoListId),
            todoResponseType,
            accessToken
        )
        val todoId = requireNotNull(todoResponse.data?.id)

        val todos = get(
            "/api/todos?listId=$todoListId",
            todoListResponseTypeList,
            accessToken
        )
        Assertions.assertThat(requireNotNull(todos.data).map { it.id }).contains(todoId)

        val toggled = post(
            "/api/todos/toggle",
            TodoToggleRequest(id = todoId),
            todoResponseType,
            accessToken
        )
        Assertions.assertThat(toggled.data?.done).isTrue()

        val deleted = post(
            "/api/todos/delete",
            TodoDeleteRequest(id = todoId, softDeleted = false),
            anyResponseType,
            accessToken
        )
        Assertions.assertThat(deleted.code).isEqualTo(0)
    }

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

    @Test
    fun `move and update todo across lists`() {
        val (accessToken, _) = registerAndLogin("move-todo@test.com")

        val listA = post(
            "/api/todoLists/add",
            TodoListAddRequest(name = "List A"),
            todoListResponseType,
            accessToken
        )
        val listB = post(
            "/api/todoLists/add",
            TodoListAddRequest(name = "List B"),
            todoListResponseType,
            accessToken
        )
        val listAId = requireNotNull(listA.data?.id)
        val listBId = requireNotNull(listB.data?.id)

        val todo = post(
            "/api/todos/add",
            TodoAddRequest(content = "Move me", dueDate = null, belongedListId = listAId),
            todoResponseType,
            accessToken
        )
        val todoId = requireNotNull(todo.data?.id)

        val moved = post(
            "/api/todos/move",
            TodoMoveRequest(id = todoId, targetListId = listBId),
            todoResponseType,
            accessToken
        )
        Assertions.assertThat(moved.data?.id).isEqualTo(todoId)

        val updated = post(
            "/api/todos/update",
            TodoUpdateRequest(
                id = todoId,
                content = "Moved and updated",
                done = true,
                dueDate = null,
                description = "Updated"
            ),
            todoResponseType,
            accessToken
        )
        Assertions.assertThat(updated.data?.done).isTrue()

        val listATodos = get(
            "/api/todos?listId=$listAId",
            todoListResponseTypeList,
            accessToken
        )
        val listBTodos = get(
            "/api/todos?listId=$listBId",
            todoListResponseTypeList,
            accessToken
        )
        Assertions.assertThat(requireNotNull(listATodos.data).map { it.id }).doesNotContain(todoId)
        Assertions.assertThat(requireNotNull(listBTodos.data).map { it.id }).contains(todoId)
    }

    @Test
    fun `delete todo list removes its todos`() {
        val (accessToken, _) = registerAndLogin("delete-list@test.com")

        val list = post(
            "/api/todoLists/add",
            TodoListAddRequest(name = "Temp"),
            todoListResponseType,
            accessToken
        )
        val listId = requireNotNull(list.data?.id)

        val todo = post(
            "/api/todos/add",
            TodoAddRequest(content = "Temp todo", dueDate = null, belongedListId = listId),
            todoResponseType,
            accessToken
        )
        val todoId = requireNotNull(todo.data?.id)

        val deleted = post(
            "/api/todoLists/delete",
            TodoListDeleteRequest(id = listId),
            anyResponseType,
            accessToken
        )
        Assertions.assertThat(deleted.code).isEqualTo(0)

        val todos = get(
            "/api/todos?listId=$listId",
            todoListResponseTypeList,
            accessToken
        )
        Assertions.assertThat(requireNotNull(todos.data).map { it.id }).doesNotContain(todoId)
    }

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

    @Test
    fun `access is denied for other users`() {
        val (tokenA, _) = registerAndLogin("owner@test.com")
        val (tokenB, _) = registerAndLogin("intruder@test.com")

        val list = post(
            "/api/todoLists/add",
            TodoListAddRequest(name = "Private"),
            todoListResponseType,
            tokenA
        )
        val listId = requireNotNull(list.data?.id)
        val todo = post(
            "/api/todos/add",
            TodoAddRequest(content = "Private todo", dueDate = null, belongedListId = listId),
            todoResponseType,
            tokenA
        )
        val todoId = requireNotNull(todo.data?.id)

        val listUpdateStatus = postForStatus(
            "/api/todoLists/update",
            TodoListUpdateRequest(id = listId, name = "Hacked"),
            tokenB
        )
        Assertions.assertThat(listUpdateStatus).isEqualTo(HttpStatus.FORBIDDEN)

        val todoToggleStatus = postForStatus(
            "/api/todos/toggle",
            TodoToggleRequest(id = todoId),
            tokenB
        )
        Assertions.assertThat(todoToggleStatus).isEqualTo(HttpStatus.FORBIDDEN)
    }

    private fun <T> post(
        path: String,
        body: Any,
        responseType: ParameterizedTypeReference<Response<T>>,
        token: String? = null
    ): Response<T> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        if (token != null) {
            headers.setBearerAuth(token)
        }
        val response = restTemplate.exchange(
            path,
            HttpMethod.POST,
            HttpEntity(body, headers),
            responseType
        )
        Assertions.assertThat(response.statusCode.is2xxSuccessful).isTrue()
        return requireNotNull(response.body)
    }

    private fun <T> get(
        path: String,
        responseType: ParameterizedTypeReference<Response<T>>,
        token: String
    ): Response<T> {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.setBearerAuth(token)

        val response = restTemplate.exchange(
            path,
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            responseType
        )
        Assertions.assertThat(response.statusCode.is2xxSuccessful).isTrue()
        return requireNotNull(response.body)
    }

    private fun postForStatus(path: String, body: Any, token: String): HttpStatus {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.setBearerAuth(token)
        val response = restTemplate.exchange<String>(
            path,
            HttpMethod.POST,
            HttpEntity(body, headers)
        )
        return response.statusCode as HttpStatus
    }

    private fun registerAndLogin(email: String): Pair<String, Long> {
        val password = "pw123456"
        val sendCode = post(
            "/api/auth/send-verify-code",
            SendVerifyCodeRequest(email),
            anyResponseType
        )
        Assertions.assertThat(sendCode.code).isEqualTo(0)

        val verificationCode = verificationCodeService.getCode(email)
        val registerResponse = post(
            "/api/auth/register",
            UserRegisterRequest(
                email = email,
                password = password,
                name = "User",
                verificationCode = verificationCode
            ),
            userResponseType
        )
        Assertions.assertThat(registerResponse.code).isEqualTo(0)

        val loginResponse = post(
            "/api/auth/login",
            UserLoginRequest(email = email, password = password),
            userResponseType
        )
        Assertions.assertThat(loginResponse.code).isEqualTo(0)
        val accessToken = requireNotNull(loginResponse.data?.tokens?.accessToken)
        val userId = requireNotNull(loginResponse.data.id)
        return accessToken to userId
    }

    companion object {
        @Container
        @JvmField
        val mysql: MySQLContainer = MySQLContainer(DockerImageName.parse("mysql:8.0.36"))
            .withDatabaseName("todo")
            .withUsername("test")
            .withPassword("test")

        @Container
        @JvmField
        val redis: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7.2.4-alpine"))
            .withExposedPorts(6379)

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
            registry.add("spring.datasource.driver-class-name") { mysql.driverClassName }
            registry.add("spring.data.redis.host") { redis.host }
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SimpleTodoListResponse(val id: Long)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SimpleTodoResponse(val id: Long, val done: Boolean)

    private val anyResponseType = object : ParameterizedTypeReference<Response<Any>>() {}
    private val userResponseType = object : ParameterizedTypeReference<Response<UserResponse>>() {}
    private val todoListResponseType = object : ParameterizedTypeReference<Response<SimpleTodoListResponse>>() {}
    private val todoResponseType = object : ParameterizedTypeReference<Response<SimpleTodoResponse>>() {}
    private val tokenResponseType = object : ParameterizedTypeReference<Response<AuthenticationTokensResponse>>() {}
    private val todoListResponseTypeList =
        object : ParameterizedTypeReference<Response<List<SimpleTodoResponse>>>() {}
}