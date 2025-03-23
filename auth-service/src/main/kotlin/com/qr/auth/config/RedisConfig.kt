package com.qr.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.*
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.*
import java.time.Duration

@Configuration
class RedisConfig {

    @Value("\${spring.data.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port}")
    private val redisPort: Int = 6379

    @Value("\${spring.data.redis.password}")
    private lateinit var redisPassword: String

    @Value("\${spring.data.redis.database}")
    private val redisDatabase: Int = 0

    @Bean
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisHost, redisPort).apply {
            password = RedisPassword.of(redisPassword)
            database = redisDatabase
        }
        return LettuceConnectionFactory(config)
    }

    @Bean
    fun jacksonRedisSerializer(objectMapper: ObjectMapper): Jackson2JsonRedisSerializer<Any> {
        val customizedMapper = objectMapper.copy().apply {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
        return Jackson2JsonRedisSerializer(customizedMapper, Any::class.java)
    }

    // 修复 3：使用正确的 ReactiveRedisTemplate 构建方式
    @Bean
    fun reactiveRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory,
        jacksonSerializer: Jackson2JsonRedisSerializer<Any>
    ): ReactiveRedisTemplate<String, Any> {
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, Any>()
            .key(StringRedisSerializer())
            .value(GenericToStringSerializer(Any::class.java))
            .hashKey(StringRedisSerializer())
            .hashValue(jacksonSerializer)
            .build()

        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }

    // 新增：用于缓存管理的阻塞式连接工厂
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisHost, redisPort)
        config.password = RedisPassword.of(redisPassword)
        config.database = redisDatabase

        return LettuceConnectionFactory(config).apply {
            afterPropertiesSet()
        }
    }

    // 修复 4：使用正确的 RedisCacheManager 构建方式
    @Bean
    fun redisCacheManager(): RedisCacheManager {
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(GenericJackson2JsonRedisSerializer())
            )

        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory()))
            .cacheDefaults(config)
            .transactionAware()
            .build()
    }

    @Bean("reactiveStringRedisTemplate")
    fun reactiveStringRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, String> {
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, String>(StringRedisSerializer())
            .build()

        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }
}