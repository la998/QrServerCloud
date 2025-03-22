package com.qr.gateway.config

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.crypto.SecretKey

@Configuration
class JwtConfig {

    @Value("\${jwt.secret}")  // 从配置文件中读取密钥
    private lateinit var secretKey: String

    @Bean
    fun secretKey(): SecretKey {
        // 将密钥字符串转换为 SecretKey
        return Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    @Bean
    fun jwtParser(secretKey: SecretKey): JwtParser {
        // 创建 JwtParser 并设置签名密钥
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
    }
}