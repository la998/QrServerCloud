package com.qr.user.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.qr.user.entity.User
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size


/**
 * 用户创建请求体（管理员专用）
 * @param login 登录用户名
 * @param hashedPassword 登录密码
 * @param roleCodes 分配的角色编码列表
 */
data class UserCreateRequest(
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 4, max = 20, message = "用户名长度需在4-20位之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    val login: String,

    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 8, max = 20, message = "密码长度需在8-20位之间")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}$",
        message = "密码必须包含字母、数字和特殊字符"
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val hashedPassword: String,

    @field:Size(min = 1, message = "至少分配一个角色")
    val roleCodes: List<String> = listOf("USER")
) {
    /**
     * 转换为用户实体（包含密码加密）
     */
    fun toEntity() = User(
        login = this.login,
        hashedPassword = this.hashedPassword, // 保持明文，服务层加密
        enabled = true,
        roles = mutableListOf() // 留空，由服务层填充
    )
}