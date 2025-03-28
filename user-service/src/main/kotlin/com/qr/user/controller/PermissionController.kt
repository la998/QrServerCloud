package com.qr.user.controller

import com.qr.user.dto.request.PermissionCreateRequest
import com.qr.user.dto.response.PermissionResponse
import com.qr.user.service.PermissionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/user/v1/permissions")
class PermissionController(
    private val permissionService: PermissionService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPermission(
        @RequestBody @Valid request: PermissionCreateRequest
    ): Mono<PermissionResponse> {
        return permissionService.createPermission(request.toEntity())
            .map { PermissionResponse.fromEntity(it) }
    }

    @GetMapping
    fun listPermissions(): Flux<PermissionResponse> {
        return permissionService.listPermissions()
            .map { PermissionResponse.fromEntity(it) }
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePermission(@PathVariable code: String): Mono<Void> {
        return permissionService.deletePermission(code)
    }
}