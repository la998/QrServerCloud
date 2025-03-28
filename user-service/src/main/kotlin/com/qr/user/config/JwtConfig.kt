package com.qr.user.config

import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.charset.StandardCharsets
import java.security.Key

@Configuration
class JwtConfig {

    @Value("\${jwt.secret-key}")
    private lateinit var secretKey: String

    @Bean
    fun jwtSecretKey(): Key {
        return Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
    }
}