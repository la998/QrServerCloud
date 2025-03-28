package com.qr.user.dto.request

import com.qr.user.entity.User
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 用户信息更新请求体
 * @param newLogin 新用户名（可选）
 * @param newPassword 新密码（可选，需通过单独接口验证旧密码）
 */
data class UserUpdateRequest(
    @field:Size(min = 4, max = 20, message = "用户名长度需在4-20位之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    val newLogin: String? = null,

    @field:Size(min = 8, max = 20, message = "密码长度需在8-20位之间")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}$",
        message = "密码必须包含字母、数字和特殊字符"
    )
    val newPassword: String? = null
) {
    /**
     * 转换为用户实体更新操作
     */
    fun toEntity() = User(
        login = newLogin,
        hashedPassword = newPassword // 保持明文，服务层加密
    )

    fun changedFields(): Map<String, Any?> {
        val changes = mutableMapOf<String, Any?>()
        if (!newLogin.isNullOrBlank()) changes["login"] = newLogin
        if (!newPassword.isNullOrBlank()) changes["hashedPassword"] = "***" // 脱敏处理
        return changes
    }
}