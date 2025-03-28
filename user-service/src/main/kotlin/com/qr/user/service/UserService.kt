package com.qr.user.service

import com.qr.user.entity.User
import com.qr.user.exception.NotFoundException
import com.qr.user.repository.UserRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @PreAuthorize("hasRole('ADMIN')")
    fun createUser(user: User): Mono<User> {
        return userRepository.existsByUsername(user.login!!)
            .flatMap { exists ->
                if (exists) {
                    Mono.error(IllegalArgumentException("Username already exists"))
                } else {
                    userRepository.save(
                        user.copy(
                            hashedPassword = passwordEncoder.encode(user.hashedPassword),
                            enabled = true
                        )
                    )
                }
            }
    }

    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    fun updateUser(id: Long, updateData: User): Mono<User> {
        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("User not found", "id", id)))
            .flatMap { existing ->
                userRepository.save(
                    existing.copy(
                        login = updateData.login ?: existing.login,
                        hashedPassword = updateData.hashedPassword?.let { passwordEncoder.encode(it) }
                            ?: existing.hashedPassword
                    )
                )
            }
    }

    @PreAuthorize("hasRole('ADMIN')")
    fun disableUser(id: Long): Mono<User> {
        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("User not found", "id", id)))
            .flatMap { userRepository.save(it.copy(enabled = false)) }
    }

    fun findAllUsers(): Flux<User> = userRepository.findAll()

    fun findByUsername(username: String): Mono<User> {
        return userRepository.findByUsername(username)
            .switchIfEmpty(Mono.error(NotFoundException("User not found", "username", username)))
    }


    fun searchUsers(keyword: String): Flux<User> {
        return userRepository.search("%$keyword%")
    }
}