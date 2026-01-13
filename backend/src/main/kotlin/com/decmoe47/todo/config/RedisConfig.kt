package com.decmoe47.todo.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer

@EnableCaching
@Configuration(proxyBeanMethods = false)
class RedisConfig {
    @Bean
    fun redisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = factory

        template.keySerializer = RedisSerializer.string()
        template.hashKeySerializer = RedisSerializer.string()

        template.valueSerializer = RedisSerializer.json()
        template.hashValueSerializer = RedisSerializer.json()

        template.afterPropertiesSet()
        return template
    }
}