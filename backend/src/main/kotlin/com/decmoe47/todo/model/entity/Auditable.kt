package com.decmoe47.todo.model.entity

import kotlinx.datetime.LocalDateTime

interface Auditable {
    val createdAt: LocalDateTime
    val createdBy: Long
    val updatedAt: LocalDateTime?
    val updatedBy: Long?
    val version: Int
    val deleted: Boolean
}
