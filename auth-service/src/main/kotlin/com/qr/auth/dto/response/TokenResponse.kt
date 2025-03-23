package com.qr.auth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * 令牌响应体
 * @param accessToken 访问令牌
 * @param refreshToken 刷新令牌
 * @param accessExpiresIn 访问令牌剩余有效时间（秒）
 * @param refreshExpiresIn 刷新令牌剩余有效时间（秒）
 */
data class TokenResponse(
    @field:JsonProperty("access_token")
    val accessToken: String,

    @field:JsonProperty("refresh_token")
    val refreshToken: String,

    @field:JsonProperty("token_type")
    val tokenType: String = "Bearer",

    @field:JsonProperty("access_expires_in")
    val accessExpiresIn: Long,

    @field:JsonProperty("refresh_expires_in")
    val refreshExpiresIn: Long,

    @field:JsonProperty("issued_at")
    val issuedAt: Instant = Instant.now()
)
