package com.qr.user.dto.response

import com.qr.user.entity.Permission
import java.time.LocalDateTime

data class PermissionResponse(
    val code: String,
    val name: String,
    val description: String?,
    val category: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun fromEntity(entity: Permission) = PermissionResponse(
            code = entity.code,
            name = entity.name,
            description = entity.description,
            category = entity.category,
            createdAt = entity.createdAt
        )
    }
}