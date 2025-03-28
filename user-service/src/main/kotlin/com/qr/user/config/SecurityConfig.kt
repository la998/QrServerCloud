package com.qr.user.config

import com.qr.user.exception.InvalidTokenException
import com.qr.user.exception.TokenExpiredException
import com.qr.user.security.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono
import org.springframework.context.annotation.Lazy

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    @Lazy private val jwtTokenProvider: JwtTokenProvider
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .authorizeExchange {
                it.pathMatchers(
                    "/api/user/v1/users/me",
                    "/api/user/v1/users/**"
                ).authenticated()
                    .anyExchange().permitAll()
            }
            .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }

    private fun jwtAuthenticationFilter(): WebFilter {
        return WebFilter { exchange, chain ->
            val token = extractToken(exchange.request)
            if (token != null) {
                jwtTokenProvider.validateToken(token)
                    .flatMap { claims ->
                        jwtTokenProvider.getAuthentication(claims)
                    }
                    .flatMap { authentication ->
                        val securityContext = SecurityContextImpl(authentication)
                        val contextRepository: ServerSecurityContextRepository = WebSessionServerSecurityContextRepository()
                        contextRepository.save(exchange, securityContext)
                            .then(chain.filter(exchange))
                    }
                    .onErrorResume { ex -> handleAuthError(ex, exchange) }
            } else {
                chain.filter(exchange)
            }
        }
    }

    private fun extractToken(request: ServerHttpRequest): String? {
        return request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)
    }

    private fun handleAuthError(ex: Throwable, exchange: ServerWebExchange): Mono<Void> {
        return when (ex) {
            is TokenExpiredException -> {
                exchange.response.setStatusCode(HttpStatus.UNAUTHORIZED)
                Mono.fromRunnable { exchange.response.setComplete() }
            }
            is InvalidTokenException -> {
                exchange.response.setStatusCode(HttpStatus.FORBIDDEN)
                Mono.fromRunnable { exchange.response.setComplete() }
            }
            else -> {
                exchange.response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                Mono.fromRunnable { exchange.response.setComplete() }
            }
        }
    }
}