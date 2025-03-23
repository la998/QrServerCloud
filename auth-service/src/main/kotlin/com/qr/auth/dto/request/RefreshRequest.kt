package com.qr.auth.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

/**
 * 刷新令牌请求体
 * @param refreshToken 刷新令牌（必须）
 */
data class RefreshRequest(
    @field:NotBlank(message = "刷新令牌不能为空")
    @field:JsonProperty("refresh_token")
    val refreshToken: String
)