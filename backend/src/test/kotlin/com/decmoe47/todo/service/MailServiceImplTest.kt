package com.decmoe47.todo.service

import com.decmoe47.todo.service.impl.MailServiceImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class MailServiceImplTest : FunSpec({
    test("send returns true and populates message fields") {
        val mailSender = mockk<JavaMailSender>()
        val messageSlot = slot<SimpleMailMessage>()
        every { mailSender.send(capture(messageSlot)) } returns Unit

        val service = MailServiceImpl(mailSender, "noreply@test.com")

        service.send(listOf("user@test.com"), "Hello", "content") shouldBe true

        messageSlot.captured.from shouldBe "noreply@test.com"
        messageSlot.captured.subject shouldBe "Hello"
        messageSlot.captured.text shouldBe "content"
        messageSlot.captured.to?.toList() shouldBe listOf("user@test.com")
        verify(exactly = 1) { mailSender.send(any<SimpleMailMessage>()) }
    }

    test("send returns false when mail sender throws") {
        val mailSender = mockk<JavaMailSender>()
        every { mailSender.send(any<SimpleMailMessage>()) } throws object : MailException("fail") {}

        val service = MailServiceImpl(mailSender, "noreply@test.com")

        service.send(listOf("user@test.com"), "Hello", "content") shouldBe false
    }
})
