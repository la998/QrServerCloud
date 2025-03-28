package com.qr.user.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.qr.user.entity.User
import java.time.LocalDateTime

/**
 * 用户信息响应体
 * @param id 用户ID
 * @param login 用户名
 * @param deviceId 设备ID（首次设备登录生成）
 * @param enabled 是否启用
 * @param createdAt 创建时间
 * @param roles 角色列表
 */
data class UserResponse(
    val id: Long,
    val login: String?,
    val deviceId: String?,
    val enabled: Boolean,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,

    val roles: List<String>
) {
    companion object {
        fun fromEntity(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                login = user.login,
                deviceId = user.deviceId,
                enabled = user.enabled,
                createdAt = user.createdAt,
                roles = user.roles
            )
        }
    }
}