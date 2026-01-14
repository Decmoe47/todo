package com.decmoe47.todo.model.entity

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEmbedded
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import java.time.LocalDateTime

@KomapperEntity
data class Todo(
    @KomapperId @KomapperAutoIncrement
    val id: Long = 0,
    val content: String,
    val dueDate: LocalDateTime?,
    val done: Boolean = false,
    val description: String? = null,
    val belongedListId: Long,
    @KomapperEmbedded
    val auditable: AuditableEntity
) : Auditable by auditable
