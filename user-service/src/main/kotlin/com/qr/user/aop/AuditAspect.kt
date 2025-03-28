package com.qr.user.aop

import com.qr.user.dto.request.UserCreateRequest
import com.qr.user.dto.request.UserUpdateRequest
import com.qr.user.entity.AuditLog
import com.qr.user.entity.User
import com.qr.user.repository.AuditRepository
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Aspect
@Component
class AuditAspect(
    private val auditRepository: AuditRepository
) {

    @Around(
        "@within(org.springframework.web.bind.annotation.RestController) && " +
                "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
                "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
                "@annotation(org.springframework.web.bind.annotation.DeleteMapping))"
    )
    fun logControllerAction(joinPoint: ProceedingJoinPoint): Mono<Any> {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap { securityContext ->
                val auth: Authentication = securityContext.authentication
                val result = joinPoint.proceed() // 执行目标方法
                if (result is Mono<*>) {
                    result.flatMap { methodResult ->
                        val auditLog = buildAuditLog(auth, joinPoint, methodResult)
                        auditRepository.save(auditLog).thenReturn(methodResult)
                    }
                } else {
                    val auditLog = buildAuditLog(auth, joinPoint, result)
                    auditRepository.save(auditLog).thenReturn(result)
                }
            }
            .onErrorResume { ex ->
                // 可选：记录错误日志
                Mono.error(ex)
            }
    }

    private fun buildAuditLog(
        authentication: Authentication,
        joinPoint: ProceedingJoinPoint,
        methodResult: Any?
    ): AuditLog {
        val user = authentication.principal as User
        return AuditLog(
            userId = user.id!!,
            action = resolveAction(joinPoint),
            targetType = resolveTargetType(joinPoint),
            targetId = resolveTargetId(joinPoint.args, methodResult),
            details = buildDetails(joinPoint.args),
            createdAt = LocalDateTime.now()
        )
    }

    private fun resolveAction(joinPoint: ProceedingJoinPoint): AuditLog.ActionType {
        return when (joinPoint.signature.name.lowercase()) {
            "createuser" -> AuditLog.ActionType.CREATE
            "updateuser" -> AuditLog.ActionType.UPDATE
            "disableuser" -> AuditLog.ActionType.DISABLE
            else -> AuditLog.ActionType.OTHER
        }
    }

    private fun resolveTargetType(joinPoint: ProceedingJoinPoint): AuditLog.TargetType? {
        return when {
            joinPoint.args.any { it is UserCreateRequest || it is UserUpdateRequest } ->
                AuditLog.TargetType.USER
            else -> null
        }
    }

    private fun resolveTargetId(args: Array<Any>, methodResult: Any?): Long? {
        // 1. 优先从方法返回结果中获取ID（如保存后的User实体）
        return (methodResult as? User)?.id
        // 2. 从方法参数中提取Long类型的ID（通常是@PathVariable）
            ?: args.filterIsInstance<Long>().firstOrNull()
            // 3. 其他可能的ID来源（根据项目实际情况扩展）
            ?: args.firstNotNullOfOrNull { arg ->
                when (arg) {
                    // 如果有其他包含ID的参数类型，在此处添加逻辑
                    else -> null
                }
            }
    }


    private fun buildDetails(args: Array<Any>): String {
        return args.joinToString(", ") {
            when (it) {
                is UserCreateRequest -> "创建用户: ${it.login}"
                is UserUpdateRequest -> "更新字段: ${it.changedFields()}"
                else -> it.toString()
            }
        }
    }

    // 假设 UserUpdateRequest 中有一个方法能返回修改的字段
    private fun UserUpdateRequest.changedFields(): String {
        return this.javaClass.declaredFields
            .filter { field ->
                field.trySetAccessible()
                field.get(this) != null
            }
            .joinToString { it.name }
    }
}