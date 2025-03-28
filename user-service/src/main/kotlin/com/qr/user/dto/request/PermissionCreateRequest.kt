package com.qr.user.dto.request

import com.qr.user.entity.Permission
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class PermissionCreateRequest(
    @field:NotBlank
    @field:Pattern(regexp = "^[a-z]+:[a-z]+$") // 格式校验：module:action
    val code: String,

    @field:NotBlank
    val name: String,

    val description: String? = null,
    val category: String = "system"
) {
    fun toEntity() = Permission(
        code = code,
        name = name,
        description = description,
        category = category
    )
}
