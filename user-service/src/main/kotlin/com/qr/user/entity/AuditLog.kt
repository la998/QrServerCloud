package com.qr.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("audit_logs")
data class AuditLog(
    @Id
    val id: Long? = null,

    @Column("user_id")
    val userId: Long,

    @Column("action")
    val action: ActionType,

    @Column("target_type")
    val targetType: TargetType? = null,

    @Column("target_id")
    val targetId: Long? = null,

    @Column("details")
    val details: String? = null,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    enum class ActionType {
        CREATE, UPDATE, DELETE, ENABLE, DISABLE, BIND, UNBIND, OTHER
    }

    enum class TargetType {
        USER, ROLE, PERMISSION, DEVICE
    }
}