package com.decmoe47.todo.config.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.mail")
data class MailProperties(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val protocol: String
)