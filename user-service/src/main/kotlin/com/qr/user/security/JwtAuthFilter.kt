package com.qr.user.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.security.Key
import java.util.stream.Collectors

@Component
class JwtAuthFilter(
    private val secretKey: Key // 注入配置的 Key Bean
) : ServerAuthenticationConverter {

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(getTokenFromRequest(exchange.request))
            .flatMap { token ->
                validateToken(token)
                    .map { claims ->
                        UsernamePasswordAuthenticationToken(
                            claims.subject,
                            null,
                            getAuthorities(claims)
                        )
                    }
            }
    }

    private fun getTokenFromRequest(request: ServerHttpRequest): String? {
        return request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)
    }

    private fun validateToken(token: String): Mono<Claims> {
        return Mono.fromCallable {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        }.onErrorResume { Mono.error(it) }
    }

    private fun getAuthorities(claims: Claims): List<SimpleGrantedAuthority> {
        return (claims["roles"] as List<*>)
            .filterIsInstance<String>()
            .map { SimpleGrantedAuthority(it) }
    }
}