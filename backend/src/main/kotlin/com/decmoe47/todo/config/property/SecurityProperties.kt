package com.decmoe47.todo.config.property

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "security")
data class SecurityProperties(
    @field:Min(-1)
    val accessTokenTtl: @Min(-1) Int,

    @field:Min(-1)
    val refreshTokenTtl: @Min(-1) Int,

    val secretKey: @NotNull String
)