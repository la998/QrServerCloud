package com.qr.user.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.server.WebFilter
import org.springframework.web.server.ServerWebExchange
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.core.io.buffer.DataBuffer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import org.reactivestreams.Publisher
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.charset.StandardCharsets
import java.time.Duration

@Configuration
@EnableWebFlux
class WebFluxConfig : WebFluxConfigurer {

    /**
     * 跨域配置（优先级低于Security配置）
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:3000", // 前端开发地址
                "https://your-production-domain.com"
            )
            .allowedMethods(
                HttpMethod.GET.toString(), // 使用toString()替代.name
                HttpMethod.POST.toString(),
                HttpMethod.PUT.toString(),
                HttpMethod.DELETE.toString(),
                HttpMethod.OPTIONS.toString()
            )
            .allowedHeaders(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT
            )
            .exposedHeaders(
                HttpHeaders.CONTENT_DISPOSITION, // 文件下载需要
                "X-Request-ID" // 自定义头
            )
            .allowCredentials(true)
            .maxAge(Duration.ofHours(1).seconds)
    }

    /**
     * 全局请求日志过滤器
     */
    @Bean
    @Order(1)
    fun requestLoggingFilter(objectMapper: ObjectMapper): WebFilter {
        return WebFilter { exchange, chain ->
            val request = exchange.request
            val path = request.uri.path
            val method = request.method
            val headers = request.headers

            // 记录请求开始时间
            val startTime = System.currentTimeMillis()

            // 打印请求日志
            println("[Request Start] ${method} $path | Headers: ${headers}")

            // 继续处理请求
            chain.filter(exchange).doFinally {
                val duration = System.currentTimeMillis() - startTime
                val status = exchange.response.statusCode?.value() ?: 0
                println("[Request End] ${method} $path | Status: $status | Duration: ${duration}ms")
            }
        }
    }

    /**
     * 全局响应包装过滤器（可选）
     */
    @Bean
    @Order(2)
    fun responseWrapperFilter(objectMapper: ObjectMapper): WebFilter {
        return WebFilter { exchange, chain ->
            val originalResponse = exchange.response

            // 创建响应装饰器（使用Spring的ServerHttpResponseDecorator）
            val decoratedResponse = object : ServerHttpResponseDecorator(originalResponse) {
                override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
                    return super.writeWith(
                        Flux.from(body)
                            .collectList()
                            .flatMap { dataBuffers ->
                                // 合并所有数据缓冲区
                                val content = dataBuffers.joinToString("") {
                                    it.toString(StandardCharsets.UTF_8)
                                }

                                // 包装响应内容
                                val wrapper = mapOf(
                                    "code" to (statusCode?.value() ?: 200),
                                    "data" to if (content.isNotBlank()) objectMapper.readValue(
                                        content,
                                        Any::class.java
                                    ) else null,
                                    "message" to "success"
                                )

                                // 转换为新的数据缓冲区
                                val newContent = objectMapper.writeValueAsString(wrapper)
                                val buffer = bufferFactory().wrap(newContent.toByteArray())
                                Mono.just(buffer)
                            }
                    )
                }
            }

            // 继续处理使用装饰后的响应
            chain.filter(exchange.mutate().response(decoratedResponse).build())
        }
    }

    /**
     * 全局异常处理过滤器（补充ControllerAdvice）
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级
    fun exceptionHandlingFilter(objectMapper: ObjectMapper): WebFilter {
        return WebFilter { exchange, chain ->
            chain.filter(exchange)
                .onErrorResume { ex ->
                    handleException(exchange.response, ex, objectMapper)
                }
        }
    }

    private fun handleException(
        response: ServerHttpResponse,
        ex: Throwable,
        objectMapper: ObjectMapper
    ): Mono<Void> {
        response.headers.contentType = MediaType.APPLICATION_JSON

        val (status, message) = when (ex) {
            is IllegalArgumentException -> HttpStatus.BAD_REQUEST to (ex.message ?: "Invalid request")
            is NoSuchElementException -> HttpStatus.NOT_FOUND to "Resource not found"
            else -> HttpStatus.INTERNAL_SERVER_ERROR to "Internal server error"
        }

        response.statusCode = status
        val errorResponse = ErrorResponse(
            code = status.value(),
            message = message
        )

        return response.writeWith(
            Mono.just(
                response.bufferFactory().wrap(
                    objectMapper.writeValueAsBytes(errorResponse)
                )
            )
        )
    }
}

// 移除自定义的ServerHttpResponseDecorator类，直接使用Spring的

data class ErrorResponse(
    val code: Int,
    val message: String
)