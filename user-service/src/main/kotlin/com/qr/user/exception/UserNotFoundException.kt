package com.qr.user.exception

class UserNotFoundException(username: String)
    : RuntimeException("用户不存在: $username")