package com.fcfs.firstcomefirstservedeventsystem.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class AppliedUserRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    fun add(userId: Long)=
        redisTemplate
            .opsForSet()
            .add("applied_user", userId.toString())
}
