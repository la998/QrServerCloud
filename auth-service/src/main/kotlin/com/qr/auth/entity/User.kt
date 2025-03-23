package com.qr.auth.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Table("users")
data class User(
    @Id
    val id: Long? = null,

    @Column("username")
    val login: String? = null,

    @Column("password")
    val hashedPassword: String? = null,

    @Column("device_id")
    val deviceId: String? = null,

    @Column("enabled")
    val enabled: Boolean = true,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = enabled

    // 实现接口方法时指向新属性名
    override fun getUsername(): String = login ?: deviceId ?: ""

    override fun getPassword(): String? = hashedPassword
}