package com.decmoe47.todo.service.impl

import com.decmoe47.todo.service.MailService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class MailServiceImpl(
    private val mailSender: JavaMailSender,

    @param:Value($$"${spring.mail.from}")
    private val from: String
) : MailService {
    override fun send(to: List<String>, subject: String, content: String): Boolean {
        val message = SimpleMailMessage()
        message.from = from
        message.setTo(*to.toTypedArray())
        message.subject = subject
        message.text = content
        try {
            mailSender.send(message)
            log.info { "邮件成功发送给${to.joinToString()}!" }
            return true
        } catch (e: MailException) {
            log.error(e) { "发送邮件失败:" }
            return false
        }
    }
}