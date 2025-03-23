package com.qr.auth.controller

import com.qr.auth.dto.request.LoginRequest
import com.qr.auth.dto.request.RefreshRequest
import com.qr.auth.dto.response.TokenResponse
import com.qr.auth.security.JwtTokenProvider
import com.qr.auth.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping("/device-login")
    fun deviceLogin(@RequestBody request: LoginRequest.DeviceLogin): Mono<ResponseEntity<TokenResponse>> {
        return authService.deviceLogin(request.deviceId)
            .map { tokenPair ->
                ResponseEntity.ok(
                    TokenResponse(
                        accessToken = tokenPair.accessToken,
                        refreshToken = tokenPair.refreshToken,
                        accessExpiresIn = tokenPair.accessExpiresIn,
                        refreshExpiresIn = tokenPair.refreshExpiresIn,
                    )
                )
            }
    }

    @PostMapping("/login")
    fun usernameLogin(@RequestBody request: LoginRequest.UsernameLogin): Mono<ResponseEntity<TokenResponse>> {
        return authService.usernameLogin(request.username, request.password)
            .map { tokenPair ->
                ResponseEntity.ok(
                    TokenResponse(
                        accessToken = tokenPair.accessToken,
                        refreshToken = tokenPair.refreshToken,
                        accessExpiresIn = tokenPair.accessExpiresIn,
                        refreshExpiresIn = tokenPair.refreshExpiresIn,
                    )
                )
            }
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshRequest): Mono<ResponseEntity<TokenResponse>> {
        return authService.refreshToken(request.refreshToken)
            .map { tokenPair ->
                ResponseEntity.ok(
                    TokenResponse(
                        accessToken = tokenPair.accessToken,
                        refreshToken = tokenPair.refreshToken,
                        accessExpiresIn =tokenPair.accessExpiresIn,
                        refreshExpiresIn = tokenPair.refreshExpiresIn,
                    )
                )
            }
    }

    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") authHeader: String): Mono<ResponseEntity<Void>> {
        val accessToken = authHeader.substringAfter("Bearer ")
        return authService.logout(accessToken)
            .thenReturn(ResponseEntity.ok().build())
    }
}