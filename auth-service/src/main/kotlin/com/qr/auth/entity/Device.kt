package com.qr.auth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("devices")
data class Device(
    @Id
    @Column("id")
    val deviceId: String,

    @Column("user_id")
    val userId: Long? = null,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)