package com.qr.auth.security

import com.qr.auth.repository.UserRepository
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ReactiveUserDetailsServiceImpl(
    private val userRepository: UserRepository
) : ReactiveUserDetailsService {

    override fun findByUsername(username: String): Mono<UserDetails> {
        return userRepository.findByUsernameOrDeviceId(username)
            .map { user ->
                User.withUsername(user.username)
                    .password(user.password)
                    .authorities("ROLE_USER") // 根据实际情况设置权限
                    .build()
            }
    }
}