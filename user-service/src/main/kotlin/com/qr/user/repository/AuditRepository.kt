package com.qr.user.repository

import com.qr.user.entity.AuditLog
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Repository
interface AuditRepository : ReactiveCrudRepository<AuditLog, Long> {

    @Query("""
        SELECT * FROM audit_logs 
        WHERE user_id = :userId 
        AND created_at BETWEEN :start AND :end 
        ORDER BY created_at DESC
        LIMIT :size OFFSET :offset
    """)
    fun findByUserAndPeriod(
        userId: Long,
        start: LocalDateTime,
        end: LocalDateTime,
        offset: Long,
        size: Int
    ): Flux<AuditLog>

    @Query("""
        SELECT * FROM audit_logs 
        WHERE action = :action 
        AND created_at >= :start 
        ORDER BY created_at DESC
    """)
    fun findByActionAfter(
        action: AuditLog.ActionType,
        start: LocalDateTime
    ): Flux<AuditLog>
}