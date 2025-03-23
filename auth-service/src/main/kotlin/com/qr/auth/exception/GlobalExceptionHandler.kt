package com.qr.auth.exception

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * ----handleJwtException完整调用流程----
 * 客户端请求
 *   → 触发JWT异常（如过期）
 *   → handleJwtException捕获异常
 *   → 生成对应AuthException
 *   → 调用handleAuthException
 *   → 返回标准化错误响应
 */
@Order(-2)
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * 处理自定义认证异常
     */
    @ExceptionHandler(AuthException::class)
    fun handleAuthException(ex: AuthException, exchange: ServerWebExchange): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.just(
            ResponseEntity.status(ex.code)
                .header(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .body(ex.toErrorResponse())
        )
    }

    /**
     * 处理JWT相关异常
     */
    @ExceptionHandler(
        ExpiredJwtException::class,
        SignatureException::class,
        MalformedJwtException::class,
        UnsupportedJwtException::class
    )
    fun handleJwtException(ex: Exception, exchange: ServerWebExchange): Mono<ResponseEntity<ErrorResponse>> { // 添加exchange参数
        val error = when (ex) {
            is ExpiredJwtException -> TokenExpiredException()
            is SignatureException -> AuthException(debugMessage = "Invalid JWT signature")
            else -> AuthException(debugMessage = ex.message)
        }
        return handleAuthException(error, exchange)
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationException(ex: WebExchangeBindException): Mono<ResponseEntity<ErrorResponse>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return Mono.just(
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    code = HttpStatus.BAD_REQUEST.value(),
                    message = "参数校验失败",
                    details = errors
                )
            )
        )
    }
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .associate { it.field to it.defaultMessage }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                code = 400,
                message = "参数校验失败",
                details = errors
            ))
    }

    /**
     * 处理Spring Security认证异常
     */
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.just(
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse(
                    code = HttpStatus.UNAUTHORIZED.value(),
                    message = "身份认证失败",
                    details = mapOf("reason" to ex.message)
                )
            )
        )
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.just(
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse(
                    code = HttpStatus.FORBIDDEN.value(),
                    message = "权限不足",
                    details = mapOf("required" to ex.message)
                )
            )
        )
    }

    /**
     * 处理响应状态异常（如手动抛出的404等）
     */
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.just(
            ResponseEntity.status(ex.statusCode).body(
                ErrorResponse(
                    code = ex.statusCode.value(),
                    message = ex.reason ?: "请求处理失败",
                    details = mapOf("debug" to ex.cause?.message)
                )
            )
        )
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception, request: ServerHttpRequest): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.just(
            ResponseEntity.internalServerError().body(
                ErrorResponse(
                    code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "服务器内部错误",
                    details = mapOf(
                        "path" to request.path.value(),
                        "error" to ex.javaClass.simpleName,
                        "trace" to ex.stackTraceToString()
                    )
                )
            )
        )
    }
}

