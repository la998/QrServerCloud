package com.qr.user.exception

import org.springframework.http.HttpStatus
import java.time.Instant

/**
 * 资源已存在异常（HTTP 409 Conflict）
 * @param resourceType 资源类型（如"User"、"Role"）
 * @param fieldName 冲突字段名（如"username"、"code"）
 * @param fieldValue 冲突字段值
 */
class AlreadyExistsException(
    resourceType: String,
    fieldName: String,
    fieldValue: Any
) : RuntimeException(
    "$resourceType with $fieldName '$fieldValue' already exists"
) {
    // 错误详情信息
    val errorDetails: Map<String, Any?> = mapOf(
        "resource" to resourceType,
        "field" to fieldName,
        "value" to fieldValue,
        "timestamp" to Instant.now()
    )

    // 对应的HTTP状态码
    val status: HttpStatus = HttpStatus.CONFLICT

    // 快速构建方法
    companion object {
        fun forUser(username: String) =
            AlreadyExistsException("User", "username", username)

        fun forRole(code: String) =
            AlreadyExistsException("Role", "code", code)

        fun forPermission(code: String) =
            AlreadyExistsException("Permission", "code", code)
    }
}