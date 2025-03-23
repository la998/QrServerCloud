package com.qr.auth.dto.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.constraints.NotBlank

/**
 * 登录请求基类（用于多态反序列化）
 * 包含两种登录方式：
 * 1. 设备登录 - DeviceLogin
 * 2. 用户名密码登录 - UsernameLogin
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = LoginRequest.DeviceLogin::class, name = "device"),
    JsonSubTypes.Type(value = LoginRequest.UsernameLogin::class, name = "username")
)
sealed class LoginRequest {

    /**
     * 设备登录请求体
     * @param deviceId 设备唯一标识（必须）
     */
    data class DeviceLogin(
        @field:NotBlank(message = "设备ID不能为空")
        val deviceId: String
    ) : LoginRequest()

    /**
     * 用户名密码登录请求体
     * @param username 用户名（必须）
     * @param password 密码（必须）
     */
    data class UsernameLogin(
        @field:NotBlank(message = "用户名不能为空")
        val username: String,

        @field:NotBlank(message = "密码不能为空")
        val password: String
    ) : LoginRequest()
}