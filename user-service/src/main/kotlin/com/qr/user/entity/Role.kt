package com.qr.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("roles")
data class Role(
    @Id
    val id: Long? = null,

    @field:JvmField
    val code: String, // 角色代码（唯一）例如：ROLE_ADMIN

    val name: String, // 角色名称 例如：系统管理员

    val description: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now()
)