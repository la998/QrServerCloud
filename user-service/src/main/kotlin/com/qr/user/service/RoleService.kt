package com.qr.user.service

import com.qr.user.dto.response.RoleResponse
import com.qr.user.entity.Role
import com.qr.user.entity.RolePermission
import com.qr.user.exception.AlreadyExistsException
import com.qr.user.exception.NotFoundException
import com.qr.user.repository.PermissionRepository
import com.qr.user.repository.RolePermissionRepository
import com.qr.user.repository.RoleRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val permissionRepository: PermissionRepository,
    private val permissionService: PermissionService
) {
    fun createRole(role: Role): Mono<Role> {
        return roleRepository.findByCode(role.code)
            .flatMap<Role> {
                Mono.error(AlreadyExistsException.forRole("角色代码已存在"))
            }
            .switchIfEmpty(roleRepository.save(role))
    }


    fun assignPermissions(roleCode: String, permissionCodes: List<String>): Mono<Void> {
        return roleRepository.findByCode(roleCode)
            .switchIfEmpty(
                Mono.error(
                    NotFoundException(
                        "Role not found",
                        "roleCode",
                        roleCode
                    )
                )
            )
            .flatMap { role ->
                permissionService.batchGetPermissions(permissionCodes).collectList()
                    .flatMap { permissions ->
                        rolePermissionRepository.deleteByRoleId(role.id!!)
                            .thenMany(
                                rolePermissionRepository.saveAll(
                                permissions.map { permission ->
                                    RolePermission(roleId = role.id!!, permissionId = permission.id!!)
                                }
                            ))
                            .then()
                    }
            }
    }

    fun getRoleWithPermissions(code: String): Mono<RoleResponse> {
        return roleRepository.findByCode(code)
            .flatMap { role ->
                rolePermissionRepository.findPermissionIdsByRoleId(role.id!!)
                    .collectList()
                    .flatMap { permissionIds ->
                        if (permissionIds.isEmpty()) {
                            // 处理权限为空的情况
                            permissionRepository.findAllById(permissionIds).collectList()
                        } else {
                            permissionRepository.findAllById(permissionIds).collectList()
                        }
                    }
                    .map { permissions ->
                        RoleResponse.fromEntity(role, permissions)
                    }
            }
    }

    fun validateRolesExist(roleCodes: List<String>): Mono<Void> {
        return roleRepository.findByCodeIn(roleCodes)
            .collectList()
            .flatMap { roles ->
                if (roles.size == roleCodes.size) Mono.empty()
                else {
                    val missing = roleCodes - roles.map { it.code }.toSet()
                    Mono.error(
                        NotFoundException(
                            "Roles not found: $missing",
                            "roleCodes",         // fieldName
                            missing.joinToString() // fieldValue
                        )
                    )
                }
            }
    }

}