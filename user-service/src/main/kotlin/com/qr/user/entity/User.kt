package com.qr.user.entity

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
    @Column("username") // 映射数据库字段
    val login: String? = null,
    @Column("password")  // 映射数据库字段
    val hashedPassword: String? = null,
    val deviceId: String? = null,
    val enabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val roles: List<String> = listOf("ROLE_USER")
) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        roles.map { SimpleGrantedAuthority(it) }.toMutableList()

    // 手动实现接口方法
    override fun getUsername(): String = login ?: deviceId ?: ""
    override fun getPassword(): String? = hashedPassword

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = enabled
}