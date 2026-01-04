package com.decmoe47.todo.service

interface VerificationCodeService {
    fun createCode(email: String): String

    fun getCode(email: String): String

    fun assertSameCode(source: String, target: String)

    fun checkCode(target: String, email: String)
}