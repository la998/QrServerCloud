package com.qr.gateway.config


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

/**
 * 跨域配置
 */
@Configuration
class CorsConfig {

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        // 1. 创建 CORS 配置对象
        val config = CorsConfiguration().apply {
            // 允许所有来源（生产环境应指定具体域名）
            addAllowedOrigin("*")

            // 允许所有请求方法
            addAllowedMethod("*")

            // 允许所有请求头
            addAllowedHeader("*")

            // 允许携带凭证（如 Cookie）
            allowCredentials = true

            // 预检请求缓存时间（秒）
            maxAge = 3600L
        }

        // 2. 注册 CORS 配置到所有路径
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }

        // 3. 返回 CORS 过滤器
        return CorsWebFilter(source)
    }
}