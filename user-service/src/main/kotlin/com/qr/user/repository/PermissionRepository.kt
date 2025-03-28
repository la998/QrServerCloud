package com.qr.user.repository

import com.qr.user.entity.Permission
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PermissionRepository : ReactiveCrudRepository<Permission, Long> {
    fun findByCode(code: String): Mono<Permission>
    fun findByCodeIn(codes: List<String>): Flux<Permission>
    fun deleteByCode(code: String): Mono<Void>
}