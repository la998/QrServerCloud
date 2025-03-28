package com.qr.user.exception

import com.alibaba.csp.sentinel.slots.block.BlockException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * 统一异常处理（响应式版本）
 * 优先级说明：
 * 1. 特定异常处理（如NotFoundException）
 * 2. 参数校验异常
 * 3. Spring Security异常
 * 4. 全局兜底异常
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyExistsException::class)
    fun handleAlreadyExistsException(
        ex: AlreadyExistsException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.just(
            ResponseEntity.status(ex.status)
                .header(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .body(
                    ErrorResponse(
                        code = ex.status.value(),
                        message = ex.message ?: "Resource already exists",
                        details = ex.errorDetails
                    )
                )
        )
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(
        ex: NotFoundException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.just(
            ResponseEntity.status(ex.status)
                .header(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .body(
                    ErrorResponse(
                        code = ex.status.value(),
                        message = ex.message ?: "Resource not found",
                        details = ex.errorDetails
                    )
                )
        )
    }

    /**
     * 处理参数校验异常（WebFlux版本）
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationException(
        ex: WebExchangeBindException
    ): Mono<ResponseEntity<ErrorResponse>> {
        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to it.defaultMessage
        }
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

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleAccessDeniedException(): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.just(
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse(
                    code = HttpStatus.FORBIDDEN.value(),
                    message = "权限不足"
                )
            )
        )
    }

    /**
     * 处理其他未捕获异常
     */
    @ExceptionHandler(Exception::class)
    fun handleGlobalException(
        ex: Exception,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.just(
            ResponseEntity.internalServerError().body(
                ErrorResponse(
                    code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "服务器内部错误",
                    details = mapOf(
                        "path" to exchange.request.path.value(),
                        "errorType" to ex.javaClass.simpleName
                    )
                )
            )
        )
    }

    @ExceptionHandler(BlockException::class) // Sentinel BlockException
    fun handleBlockException(ex: BlockException): Mono<ResponseEntity<ErrorResponse>> {
        return Mono.just(
            ResponseEntity.status(429).body(
                ErrorResponse(
                    code = 429,
                    message = "请求过于频繁"
                )
            )
        )
    }
}

