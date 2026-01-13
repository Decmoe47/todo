package com.decmoe47.todo.service

interface MailService {
    fun send(to: List<String>, subject: String, content: String): Boolean
}