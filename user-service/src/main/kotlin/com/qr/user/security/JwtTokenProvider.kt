package com.qr.user.security

import com.qr.user.entity.User
import com.qr.user.exception.InvalidTokenException
import com.qr.user.exception.TokenExpiredException
import com.qr.user.exception.UserNotFoundException
import com.qr.user.service.UserService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.security.Key
import java.time.Duration
import java.time.Instant
import java.util.*
import org.springframework.context.annotation.Lazy

@Component
class JwtTokenProvider(
    @Lazy
    private val userService: UserService,
    private val secretKey: Key, // 注入配置的 Key Bean
    @Value("\${jwt.access-token-expire}")
    private val accessTokenExpire: Duration,
    @Value("\${jwt.refresh-token-expire}")
    private val refreshTokenExpire: Duration,
) {

    fun generateAccessToken(authentication: Authentication): String {
        val user = authentication.principal as User
        val now = Instant.now()

        return Jwts.builder()
            .setSubject(user.username ?: user.deviceId)
            .claim("roles", user.roles)
            .claim("perms", getPermissions(user))
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now + accessTokenExpire))
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(authentication: Authentication): String {
        val now = Instant.now()
        return Jwts.builder()
            .setSubject(authentication.name)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now + refreshTokenExpire))
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Mono<Claims> {
        return Mono.fromCallable {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        }.onErrorMap { ex ->
            when (ex) {
                is io.jsonwebtoken.ExpiredJwtException -> TokenExpiredException()
                is io.jsonwebtoken.security.SignatureException -> InvalidTokenException("无效签名")
                else -> InvalidTokenException("令牌解析失败", ex)
            }
        }
    }

    fun getAuthentication(claims: Claims): Mono<Authentication> {
        return userService.findByUsername(claims.subject)
            .switchIfEmpty(Mono.error(UserNotFoundException(claims.subject)))
            .map { user ->
                UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    getAuthorities(claims)
                )
            }
    }

    private fun getPermissions(user: User): List<String> {
        return listOf("user:read", "user:write")
    }

    private fun getAuthorities(claims: Claims): List<GrantedAuthority> {
        val perms = claims.get("perms", List::class.java) as List<String>
        return perms.map { SimpleGrantedAuthority(it) }
    }
}