package com.qr.user.repository

import com.qr.user.entity.User
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserRepository : ReactiveCrudRepository<User, Long> {
    fun findByUsername(username: String): Mono<User>
    fun findByDeviceId(deviceId: String): Mono<User>
    fun existsByUsername(username: String): Mono<Boolean>

    @Query("SELECT * FROM users WHERE username LIKE :keyword OR device_id LIKE :keyword")
    fun search(keyword: String): Flux<User>
}