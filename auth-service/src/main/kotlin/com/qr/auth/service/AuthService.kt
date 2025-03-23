package com.qr.auth.service

import com.qr.auth.dto.response.TokenResponse
import com.qr.auth.entity.User
import com.qr.auth.exception.AuthException
import com.qr.auth.repository.UserRepository
import com.qr.auth.security.JwtTokenProvider
import io.jsonwebtoken.Claims
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.util.function.Tuple4
import java.time.Duration
import java.time.Instant
import java.util.*


@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val tokenBlacklistService: TokenBlacklistService
) {

    /**
     * 设备登录（首次自动注册）
     * @param deviceId 设备唯一标识
     */
    fun deviceLogin(deviceId: String): Mono<TokenResponse> {
        return userRepository.findByDeviceId(deviceId)
            .switchIfEmpty(createNewDeviceUser(deviceId)) // 设备不存在则创建新用户
            .flatMap { user ->
                if (!user.enabled) {
                    Mono.error(AuthException(400,"用户已被禁用"))
                } else {
                    generateTokenResponse(user)
                }
            }
    }

    /**
     * 用户名密码登录
     * @param username 用户名
     * @param password 密码
     */
    fun usernameLogin(username: String, password: String): Mono<TokenResponse> {
        return userRepository.findByUsernameOrDeviceId(username)
            .switchIfEmpty(Mono.error(AuthException(400,"用户不存在")))
            .flatMap { user ->
                if (!passwordEncoder.matches(password, user.password)) {
                    Mono.error(AuthException(400,"密码错误"))
                } else if (!user.enabled) {
                    Mono.error(AuthException(400,"用户已被禁用"))
                } else {
                    generateTokenResponse(user)
                }
            }
    }

    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     */
    fun refreshToken(refreshToken: String): Mono<TokenResponse> {
        return jwtTokenProvider.validateToken(refreshToken)
            .flatMap { claims ->
                val username = claims.subject
                userRepository.findByUsernameOrDeviceId(username)
                    .switchIfEmpty(Mono.error(AuthException(400,"用户不存在")))
                    .flatMap { user ->
                        checkRefreshTokenInRedis(refreshToken, user.deviceId)
                            .then(generateTokenResponse(user))
                            .doOnNext { invalidateOldRefreshToken(refreshToken) }
                    }
            }
    }

    /**
     * 用户登出
     * @param accessToken 当前访问令牌
     */
    fun logout(accessToken: String): Mono<Void> {
        return jwtTokenProvider.validateToken(accessToken)
            .flatMap { claims ->
                val refreshTokenKey = "refresh:${claims.subject}"

                redisTemplate.opsForValue().get(refreshTokenKey)
                    .flatMap { refreshToken ->
                        Mono.zip(
                            tokenBlacklistService.addToBlacklist(
                                accessToken,
                                getTokenRemainingTime(claims)
                            ),
                            jwtTokenProvider.validateToken(refreshToken)
                                .flatMap { refreshClaims ->
                                    tokenBlacklistService.addToBlacklist(
                                        refreshToken,
                                        getTokenRemainingTime(refreshClaims)
                                    )
                                }
                        ).then()
                    }
            }
    }

    //--- 私有工具方法 ---//

    /**
     * 创建新设备用户
     */
    private fun createNewDeviceUser(deviceId: String): Mono<User> {
        return userRepository.save(
            User(
                deviceId = deviceId,
                login = passwordEncoder.encode(UUID.randomUUID().toString()) // 生成随机初始密码
            )
        )
    }

    /**
     * 生成令牌响应
     */
    private fun generateTokenResponse(user: User): Mono<TokenResponse> {
        val authentication = createAuthentication(user)

        // 明确转换每个 Mono 的类型
        val accessTokenMono: Mono<String> = jwtTokenProvider.createAccessToken(authentication)
        val refreshTokenMono: Mono<String> = jwtTokenProvider.createRefreshToken(authentication)
        val accessExpireMono: Mono<Long> = Mono.just(jwtTokenProvider.getAccessTokenExpiration().seconds)
            .map { it.toLong() } // 如果 seconds 是 Int，需转为 Long
        val refreshExpireMono: Mono<Long> = Mono.just(jwtTokenProvider.getRefreshTokenExpiration().seconds)
            .map { it.toLong() }

        return Mono.zip(
            accessTokenMono,
            refreshTokenMono,
            accessExpireMono,
            refreshExpireMono
        ).flatMap { tuple4: Tuple4<String, String, Long, Long> ->
            storeRefreshToken(user.deviceId!!, tuple4.t2)
                .thenReturn(
                    TokenResponse(
                        accessToken = tuple4.t1,
                        refreshToken = tuple4.t2,
                        accessExpiresIn = tuple4.t3,
                        refreshExpiresIn = tuple4.t4
                    )
                )
        }
    }


    /**
     * 存储刷新令牌到Redis
     */
    private fun storeRefreshToken(deviceId: String, refreshToken: String): Mono<Boolean> {
        return redisTemplate.opsForValue().set(
            "refresh:$deviceId",
            refreshToken,
            Duration.ofSeconds(jwtTokenProvider.getRefreshTokenExpiration().seconds)
        )
    }

    /**
     * 验证刷新令牌有效性
     */
    private fun checkRefreshTokenInRedis(refreshToken: String, deviceId: String?): Mono<Boolean> {
        return deviceId?.let { id ->
            redisTemplate.opsForValue().get("refresh:$id")
                .flatMap { storedToken ->
                    if (storedToken == refreshToken) {
                        Mono.just(true)
                    } else {
                        Mono.error(AuthException(400,"刷新令牌已失效"))
                    }
                }
        } ?: Mono.error(AuthException(400,"设备未绑定"))
    }

    /**
     * 使旧刷新令牌失效
     */
    private fun invalidateOldRefreshToken(oldRefreshToken: String) {
        jwtTokenProvider.validateToken(oldRefreshToken)
            .flatMap { claims ->
                tokenBlacklistService.addToBlacklist(
                    oldRefreshToken,
                    Duration.between(Instant.now(), claims.expiration.toInstant())
                )
            }
            .subscribe()
    }

    /**
     * 计算令牌剩余有效时间
     */
    private fun getTokenRemainingTime(claims: Claims): Duration {
        return Duration.between(Instant.now(), claims.expiration.toInstant())
    }

    private fun createAuthentication(user: User): Authentication {
        return UsernamePasswordAuthenticationToken(
            user.username ?: user.deviceId,
            null,
            user.authorities
        )
    }
}