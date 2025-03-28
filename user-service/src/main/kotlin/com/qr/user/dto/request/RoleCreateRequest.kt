package com.qr.user.dto.request

import com.qr.user.entity.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class RoleCreateRequest(
    @field:NotBlank
    @field:Pattern(regexp = "^ROLE_[A-Z]+$") // 格式校验：ROLE_ADMIN
    val code: String,

    @field:NotBlank
    val name: String,
    val description: String? = null
) {
    fun toEntity() = Role(
        code = code,
        name = name,
        description = description
    )
}