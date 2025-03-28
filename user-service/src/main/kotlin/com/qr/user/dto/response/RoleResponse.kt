package com.qr.user.dto.response

import com.qr.user.entity.Permission
import com.qr.user.entity.Role
import java.time.LocalDateTime

data class RoleResponse(
    val code: String,
    val name: String,
    val description: String?,
    val permissions: List<PermissionResponse>,
    val createdAt: LocalDateTime
) {
    companion object {
        fun fromEntity(role: Role, permissions: List<Permission>) = RoleResponse(
            code = role.code,
            name = role.name,
            description = role.description,
            permissions = permissions.map { PermissionResponse.fromEntity(it) },
            createdAt = role.createdAt
        )
    }
}