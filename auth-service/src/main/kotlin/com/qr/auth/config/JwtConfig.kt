package com.qr.auth.config

import com.qr.auth.security.JwtTokenProvider
import com.qr.auth.security.ReactiveUserDetailsServiceImpl
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey
import java.time.Duration

@Configuration
class JwtConfig {

    @Value("\${jwt.secret-key}")
    private lateinit var secretKey: String

    @Value("\${jwt.access-token-expire}")
    private lateinit var accessTokenExpireString: String

    @Value("\${jwt.refresh-token-expire}")
    private lateinit var refreshTokenExpireString: String

    @Bean
    fun accessTokenExpire(): Duration {
        return Duration.parse(accessTokenExpireString)
    }

    @Bean
    fun refreshTokenExpire(): Duration {
        return Duration.parse(refreshTokenExpireString)
    }

    @Bean
    fun jwtSecretKey(): SecretKey {
        return Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
    }

    @Bean
    fun jwtTokenProvider(
        secretKey: SecretKey,
        userDetailsService: ReactiveUserDetailsServiceImpl
    ) = JwtTokenProvider(
        accessTokenExpire = accessTokenExpire(),
        refreshTokenExpire = refreshTokenExpire(),
        secretKey = secretKey,
        userDetailsService = userDetailsService
    )
}