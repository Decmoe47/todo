package com.decmoe47.todo.integrationtest

import com.decmoe47.todo.model.request.SendVerifyCodeRequest
import com.decmoe47.todo.model.request.UserLoginRequest
import com.decmoe47.todo.model.request.UserRegisterRequest
import com.decmoe47.todo.model.response.AuthenticationTokensResponse
import com.decmoe47.todo.model.response.Response
import com.decmoe47.todo.model.response.UserResponse
import com.decmoe47.todo.service.VerificationCodeService
import com.decmoe47.todo.test.TestMailConfig
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.assertj.core.api.Assertions
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
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestMailConfig::class)
@Testcontainers
abstract class IntegrationTestBase {

    @Autowired
    protected lateinit var restTemplate: TestRestTemplate

    @Autowired
    protected lateinit var verificationCodeService: VerificationCodeService

    protected fun <T> post(
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

    protected fun <T> get(
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

    protected fun postForStatus(path: String, body: Any, token: String): HttpStatus {
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

    protected fun registerAndLogin(email: String): Pair<String, Long> {
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
        @JvmStatic
        val mysql: MySQLContainer = MySQLContainer(DockerImageName.parse("mysql:8.0.36"))
            .withDatabaseName("todo")
            .withUsername("test")
            .withPassword("test")

        @JvmStatic
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

        init {
            // https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
            mysql.start()
            redis.start()
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SimpleTodoListResponse(val id: Long)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SimpleTodoResponse(val id: Long, val done: Boolean)

    protected val anyResponseType = object : ParameterizedTypeReference<Response<Any>>() {}
    protected val userResponseType = object : ParameterizedTypeReference<Response<UserResponse>>() {}
    protected val todoListResponseType = object : ParameterizedTypeReference<Response<SimpleTodoListResponse>>() {}
    protected val todoResponseType = object : ParameterizedTypeReference<Response<SimpleTodoResponse>>() {}
    protected val tokenResponseType = object : ParameterizedTypeReference<Response<AuthenticationTokensResponse>>() {}
    protected val todoListResponseTypeList =
        object : ParameterizedTypeReference<Response<List<SimpleTodoResponse>>>() {}
}
