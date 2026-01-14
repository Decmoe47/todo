package com.decmoe47.todo.model.entity

import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import java.time.LocalDateTime

data class AuditableEntity(
    @KomapperCreatedAt
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val createdBy: Long = 0,
    @KomapperUpdatedAt
    override val updatedAt: LocalDateTime? = null,
    override val updatedBy: Long? = null,
    @KomapperVersion
    override val version: Int = 0,
    override val deleted: Boolean = false,
) : Auditable
