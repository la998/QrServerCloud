package com.qr.user.controller

import com.qr.user.dto.request.UserCreateRequest
import com.qr.user.dto.request.UserUpdateRequest
import com.qr.user.dto.response.UserResponse
import com.qr.user.entity.AuditLog
import com.qr.user.entity.User
import com.qr.user.exception.NotFoundException
import com.qr.user.repository.AuditRepository
import com.qr.user.repository.UserRepository
import com.qr.user.service.RoleService
import com.qr.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.Principal
import java.time.LocalDateTime
import org.springframework.security.core.context.SecurityContextHolder

@RestController
@RequestMapping("/api/user/v1/users")
class UserController(
    private val userService: UserService,
    private val roleService: RoleService,
    private val passwordEncoder: PasswordEncoder,
    private val auditRepository: AuditRepository,
    private val userRepository: UserRepository,
) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createUser(
        @Valid @RequestBody request: UserCreateRequest,
        principal: Principal
    ): Mono<UserResponse> {
        return roleService.validateRolesExist(request.roleCodes)
            .then(userService.createUser(request.toEntity()))
            .map { UserResponse.fromEntity(it) }
    }

    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    fun updateUser(
        id: Long,
        updateData: User,
        oldPassword: String? = null // 新增旧密码参数
    ): Mono<User> {
        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("User not found", "id", id)))
            .flatMap { existing ->
                // 如果请求中包含新密码，则验证旧密码
                if (updateData.password != null && oldPassword == null) {
                    Mono.error(IllegalArgumentException("旧密码不能为空"))
                } else if (updateData.password != null && oldPassword != null) {
                    if (!passwordEncoder.matches(oldPassword, existing.password)) {
                        Mono.error(IllegalArgumentException("旧密码错误"))
                    } else {
                        val updatedUser = existing.copy(
                            login = updateData.username ?: existing.username,
                            hashedPassword = passwordEncoder.encode(updateData.password)
                        )
                        userRepository.save(updatedUser)
                    }
                } else {
                    val updatedUser = existing.copy(
                        login = updateData.login ?: existing.login
                    )
                    userRepository.save(updatedUser)
                }
            }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun disableUser(@PathVariable id: Long): Mono<Void> {
        return userService.disableUser(id).then()
    }

    @GetMapping("/me")
    fun getCurrentUser(principal: Principal): Mono<UserResponse> {
        return userService.findByUsername(principal.name)
            .map { UserResponse.fromEntity(it) }
    }

    @GetMapping
    fun listUsers(@RequestParam(required = false) keyword: String?): Flux<UserResponse> {
        return (keyword?.let { userService.searchUsers(it) } ?: userService.findAllUsers())
            .map { UserResponse.fromEntity(it) }
    }

    @GetMapping("/audit-logs")
    fun getAuditLogs(
        @RequestParam(required = false) operationType: AuditLog.ActionType?,
        @RequestParam startTime: LocalDateTime,
        @RequestParam endTime: LocalDateTime,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Flux<AuditLog> {
        return auditRepository.findByUserAndPeriod(
            getCurrentUserId(),
            startTime,
            endTime,
            page.toLong() * size,
            size
        ).filter { log ->
            operationType?.let { log.action == it } ?: true
        }
    }

    private fun getCurrentUserId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = authentication.principal as User
        return principal.id!!
    }
}