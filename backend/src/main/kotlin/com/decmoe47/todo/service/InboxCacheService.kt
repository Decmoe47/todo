package com.decmoe47.todo.service

interface InboxCacheService {
    fun getInboxId(userId: Long): Long
}
