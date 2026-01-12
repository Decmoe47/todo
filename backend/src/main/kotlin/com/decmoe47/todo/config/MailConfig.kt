package com.decmoe47.todo.config

import com.decmoe47.todo.config.property.MailProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration(proxyBeanMethods = false)
class MailConfig(private val cfg: MailProperties) {
    @Bean
    fun javaMailSender(): JavaMailSender {
        val sender = JavaMailSenderImpl()
        sender.host = cfg.host
        sender.port = cfg.port
        sender.username = cfg.username
        sender.password = cfg.password
        sender.defaultEncoding = "Utf-8"
        sender.protocol = cfg.protocol
        return sender
    }
}
