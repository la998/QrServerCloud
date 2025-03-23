package com.qr.auth.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class TokenBlacklistService(
    @Qualifier("reactiveStringRedisTemplate")
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {
    fun addToBlacklist(token: String, ttl: Duration): Mono<Boolean> {
        return redisTemplate.opsForValue().set(
            "blacklist:$token",
            "1",
            ttl
        )
    }

    fun isTokenBlacklisted(token: String): Mono<Boolean> {
        return redisTemplate.opsForValue().get("blacklist:$token")
            .map { true }
            .defaultIfEmpty(false)
    }
}