package com.qr.auth.security

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)