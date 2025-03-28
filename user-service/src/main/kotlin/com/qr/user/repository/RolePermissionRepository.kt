package com.qr.user.repository

import com.qr.user.entity.RolePermission
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface RolePermissionRepository : ReactiveCrudRepository<RolePermission, Long> {
    @Query("SELECT permission_id FROM role_permissions WHERE role_id = :roleId")
    fun findPermissionIdsByRoleId(roleId: Long): Flux<Long>
    fun deleteByRoleId(roleId: Long): Mono<Void>
    fun deleteByRoleIdAndPermissionId(roleId: Long, permissionId: Long): Mono<Void>
}