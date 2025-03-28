package com.qr.user.exception

import org.springframework.http.HttpStatus
import java.time.Instant

/**
 * 资源未找到异常
 * @param resourceName 资源类型名称（如"User"）
 * @param fieldName 查询字段名（如"id"）
 * @param fieldValue 查询字段值
 */
class NotFoundException(
    resourceName: String,
    fieldName: String,
    fieldValue: Any
) : RuntimeException(
    "$resourceName not found with $fieldName: '$fieldValue'"
) {
    val errorDetails: Map<String, Any?> = mapOf(
        "resource" to resourceName,
        "field" to fieldName,
        "value" to fieldValue,
        "timestamp" to Instant.now()
    )

    val status: HttpStatus = HttpStatus.NOT_FOUND
}