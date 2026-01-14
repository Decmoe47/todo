package com.decmoe47.todo.model.entity

import java.time.LocalDateTime

interface Auditable {
    val createdAt: LocalDateTime
    val createdBy: Long
    val updatedAt: LocalDateTime?
    val updatedBy: Long?
    val version: Int
    val deleted: Boolean
}
