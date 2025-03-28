package com.qr.user.exception

class InvalidTokenException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)