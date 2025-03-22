package com.qr.gateway.filter


import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import io.jsonwebtoken.JwtParser
import org.springframework.data.redis.core.ReactiveRedisTemplate
import java.util.Date

/**
 * JWT鉴权过滤器（Kotlin响应式实现）
 */
@Component
class JwtAuthFilter(
    private val jwtParser: JwtParser,
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) : GlobalFilter {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.path.value()

        // 白名单路径检查
        if (path.startsWith("/api/auth/")) {
            return chain.filter(exchange)
        }

        return extractToken(request)
            .flatMap { token ->
                validateToken(token)
                    .flatMap { valid ->
                        if (valid) chain.filter(exchange)
                        else sendError(exchange, HttpStatus.UNAUTHORIZED, "Invalid token")
                    }
            }
            .switchIfEmpty(sendError(exchange, HttpStatus.UNAUTHORIZED, "Missing token"))
    }

    private fun extractToken(request: ServerHttpRequest): Mono<String> {
        return Mono.justOrEmpty(request.headers.getFirst(HttpHeaders.AUTHORIZATION))
            .filter { it.startsWith("Bearer ") }
            .map { it.substring(7) }
    }

    private fun validateToken(token: String): Mono<Boolean> {
        return try {
            val claims = jwtParser.parseSignedClaims(token)
            val expiration = claims.payload.expiration
            val isExpired = expiration.before(Date())

            redisTemplate.opsForValue().get("blacklist:$token")
                .map { it == null && !isExpired }
        } catch (e: Exception) {
            Mono.just(false)
        }
    }

    private fun sendError(exchange: ServerWebExchange, status: HttpStatus, message: String): Mono<Void> {
        exchange.response.statusCode = status
        exchange.response.headers.contentType = MediaType.APPLICATION_JSON
        val dataBuffer = exchange.response.bufferFactory()
            .wrap("{\"code\":${status.value()},\"message\":\"$message\"}".toByteArray())
        return exchange.response.writeWith(Mono.just(dataBuffer))
    }
}