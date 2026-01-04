package com.decmoe47.todo.model.entity

import kotlinx.datetime.LocalDateTime
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEmbedded
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId

@KomapperEntity
data class User(
    @KomapperId @KomapperAutoIncrement
    val id: Long = 0,
    val email: String,
    val password: String,
    val name: String,
    val lastLoginTime: LocalDateTime? = null,
    @KomapperEmbedded
    val auditable: AuditableEntity = AuditableEntity()
) : Auditable by auditable
