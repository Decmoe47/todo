package com.decmoe47.todo.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class MailConfig(
    @param:Value($$"${spring.mail.host}")
    private val host: String,

    @param:Value($$"${spring.mail.port}")
    private val port: Int = 0,

    @param:Value($$"${spring.mail.username}")
    private val username: String,

    @param:Value($$"${spring.mail.password}")
    private val password: String,

    @param:Value($$"${spring.mail.protocol}")
    private val protocol: String
) {
    @Bean
    fun javaMailSender(): JavaMailSender {
        val sender = JavaMailSenderImpl()
        sender.host = host
        sender.port = port
        sender.username = username
        sender.password = password
        sender.defaultEncoding = "Utf-8"
        sender.protocol = protocol
        return sender
    }
}