package com.qr.auth.config

import com.qr.auth.security.JwtTokenProvider
import com.qr.auth.security.ReactiveUserDetailsServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

/**
 * 客户端请求 -> Gateway -> JWT过滤器 -> 认证管理器 -> UserDetails服务 -> 生成认证对象
 */

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: ReactiveUserDetailsServiceImpl
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(): ReactiveAuthenticationManager {
        return UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)
    }

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        authenticationManager: ReactiveAuthenticationManager
    ): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .authorizeExchange {
                it.pathMatchers(
                    "/api/auth/device-login",
                    "/api/auth/login",
                    "/api/auth/refresh"
                ).permitAll()
                    .anyExchange().authenticated()
            }
            .addFilterAt(
                AuthenticationWebFilter(authenticationManager).apply {
                    setServerAuthenticationConverter(jwtTokenProvider) // 设置JWT转换器
                },
                SecurityWebFiltersOrder.AUTHENTICATION
            )
            .build()
    }

}