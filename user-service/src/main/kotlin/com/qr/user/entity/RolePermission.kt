package com.qr.user.entity

import org.springframework.data.relational.core.mapping.Table


@Table("role_permissions")
data class RolePermission(
    val roleId: Long,
    val permissionId: Long
)