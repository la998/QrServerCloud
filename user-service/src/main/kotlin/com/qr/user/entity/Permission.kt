package com.qr.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("permissions")
data class Permission(
    @Id
    val id: Long? = null,

    @field:JvmField // 解决Kotlin属性名与字段名映射问题
    val code: String, // 权限代码（唯一）例如：user:read

    val name: String, // 权限名称 例如：查看用户

    val description: String? = null,

    val category: String = "system", // 权限分类

    val createdAt: LocalDateTime = LocalDateTime.now()
)