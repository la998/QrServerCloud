package com.qr.auth.exception

import java.time.Instant

/**
 * 统一错误响应结构
 */
data class ErrorResponse(
    val code: Int,
    val message: String,
    val timestamp: Instant = Instant.now(),
    val details: Map<String, Any?> = emptyMap()
)