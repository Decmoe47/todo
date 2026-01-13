package com.decmoe47.todo.test

import com.decmoe47.todo.service.MailService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestMailConfig {
    @Bean
    @Primary
    fun mailService(): MailService {
        return object : MailService {
            override fun send(to: List<String>, subject: String, content: String): Boolean = true
        }
    }
}
