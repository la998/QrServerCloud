package com.qr.user.controller

import com.qr.user.dto.request.RoleCreateRequest
import com.qr.user.dto.response.RoleResponse
import com.qr.user.entity.Role
import com.qr.user.service.RoleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/user/v1/roles")
class RoleController(
    private val roleService: RoleService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRole(
        @RequestBody @Valid request: RoleCreateRequest
    ): Mono<Role> {
        return roleService.createRole(request.toEntity())
    }

    @PostMapping("/{roleCode}/permissions")
    fun assignPermissions(
        @PathVariable roleCode: String,
        @RequestBody codes: List<String>
    ): Mono<Void> {
        return roleService.assignPermissions(roleCode, codes)
    }

    @GetMapping("/{code}")
    fun getRoleDetails(@PathVariable code: String): Mono<RoleResponse> {
        return roleService.getRoleWithPermissions(code)
    }
}