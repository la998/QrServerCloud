package com.qr.user.repository

import com.qr.user.entity.Role
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface RoleRepository : ReactiveCrudRepository<Role, Long> {
    fun findByCode(code: String): Mono<Role>
    fun findByCodeIn(codes: List<String>): Flux<Role>
}