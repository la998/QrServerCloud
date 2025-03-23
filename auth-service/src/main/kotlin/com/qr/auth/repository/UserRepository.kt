package com.qr.auth.repository

import com.qr.auth.entity.User
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface UserRepository : ReactiveCrudRepository<User, Long> {

    @Query("SELECT * FROM users WHERE username = :username OR device_id = :identity")
    fun findByUsernameOrDeviceId(identity: String): Mono<User>

    @Query("SELECT * FROM users WHERE device_id = :deviceId")
    fun findByDeviceId(deviceId: String): Mono<User>
}