package com.qr.user.service

import com.qr.user.entity.Permission
import com.qr.user.exception.AlreadyExistsException
import com.qr.user.repository.PermissionRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PermissionService(
    private val permissionRepository: PermissionRepository
) {
    fun createPermission(permission: Permission): Mono<Permission> {
        return permissionRepository.findByCode(permission.code)
            .flatMap<Permission> {
                Mono.error(AlreadyExistsException.forPermission("权限代码已存在"))
            }
            .switchIfEmpty(permissionRepository.save(permission))
    }

    fun batchGetPermissions(codes: List<String>): Flux<Permission> {
        return permissionRepository.findByCodeIn(codes)
    }

    fun listPermissions(): Flux<Permission> {
        return permissionRepository.findAll()
    }

    fun deletePermission(code: String): Mono<Void> {
        return permissionRepository.deleteByCode(code)
    }
}