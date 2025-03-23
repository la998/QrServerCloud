package com.qr.auth.security

import com.qr.auth.exception.AuthException
import io.jsonwebtoken.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val accessTokenExpire: Duration,
    private val refreshTokenExpire: Duration,
    private val secretKey: SecretKey,
    private val userDetailsService: ReactiveUserDetailsService
) : ServerAuthenticationConverter {

    // 修复点1：同步生成Token
    fun createAccessToken(authentication: Authentication): Mono<String> {
        val now = Instant.now()
        return Mono.fromCallable {
            Jwts.builder()
                .setSubject(authentication.name)
                .claim("roles", authentication.authorities.map { it.authority })
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessTokenExpire)))
                .signWith(secretKey)
                .compact()
        }.onErrorMap { ex ->
            AuthException(500, "Token generation failed", ex.message)
        }
    }

    fun createRefreshToken(authentication: Authentication): Mono<String> {
        val now = Instant.now()
        return Mono.fromCallable {
            Jwts.builder()
                .setSubject(authentication.name)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(refreshTokenExpire)))
                .signWith(secretKey)
                .compact()
        }.onErrorMap { ex ->
            AuthException(500, "Refresh token generation failed", ex.message)
        }

    }

    // 修复点3：空安全处理
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(resolveToken(exchange))
            .flatMap { token ->
                validateToken(token).flatMap { claims ->
                    userDetailsService.findByUsername(claims.subject)
                        .map { user ->
                            UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                getAuthorities(claims)
                            )
                        }
                }
            }
    }

    // 修复点4：增强Token解析
    private fun resolveToken(exchange: ServerWebExchange): String? {
        return exchange.request.headers.getFirst("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substringAfter("Bearer ")
    }

    // 修复点5：异常处理优化
    fun validateToken(token: String): Mono<Claims> = Mono.fromCallable {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (ex: ExpiredJwtException) {
            throw AuthException(401, "Token expired", ex.message)
        } catch (ex: UnsupportedJwtException) {
            throw AuthException(400, "Unsupported token", ex.message)
        } catch (ex: MalformedJwtException) {
            throw AuthException(400, "Malformed token", ex.message)
        } catch (ex: SignatureException) {
            throw AuthException(400, "Invalid signature", ex.message)
        } catch (ex: IllegalArgumentException) {
            throw AuthException(400, "Invalid token", ex.message)
        }
    }.onErrorMap { ex ->
        when (ex) {
            is AuthException -> ex
            else -> AuthException(500, "Token validation failed", ex.message)
        }
    }

    // 修复点6：权限解析实现
    private fun getAuthorities(claims: Claims): Collection<GrantedAuthority> {
        return claims.get("roles", List::class.java)
            ?.filterIsInstance<String>()
            ?.map { SimpleGrantedAuthority(it) }
            ?: emptyList()
    }

    // 辅助方法
    fun getAccessTokenExpiration() = accessTokenExpire
    fun getRefreshTokenExpiration() = refreshTokenExpire
}