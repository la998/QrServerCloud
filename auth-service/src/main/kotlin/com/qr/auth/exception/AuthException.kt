package com.qr.auth.exception

import org.springframework.http.HttpStatus
import java.time.Instant

/**
 * 认证服务专用异常
 * @param code 错误码（与HTTP状态码对应）
 * @param message 客户端可见的错误描述
 * @param debugMessage 调试用详细信息
 */
open class AuthException(
    val code: Int = HttpStatus.UNAUTHORIZED.value(),
    message: String = "认证失败",
    val debugMessage: String? = null
) : RuntimeException(message) {

    /** 错误发生时间 */
    val timestamp: Instant = Instant.now()

    /** 构建标准错误响应 */
    fun toErrorResponse() = message?.let {
        ErrorResponse(
        code = code,
        message = it,
        timestamp = timestamp,
        details = mapOf("debug" to debugMessage)
    )
    }
}

/**
 * 扩展异常类型（按需添加）
 */
class TokenExpiredException : AuthException(
    code = HttpStatus.UNAUTHORIZED.value(),
    message = "令牌已过期",
    debugMessage = "JWT token expired"
)

class InvalidTokenException : AuthException(
    code = HttpStatus.UNAUTHORIZED.value(),
    message = "无效令牌",
    debugMessage = "Malformed JWT token"
)