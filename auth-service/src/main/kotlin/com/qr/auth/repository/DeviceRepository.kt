package com.qr.auth.repository

import com.qr.auth.entity.Device
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface DeviceRepository : ReactiveCrudRepository<Device, String> {

    fun findByDeviceId(deviceId: String): Mono<Device>
}