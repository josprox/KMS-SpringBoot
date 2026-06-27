package com.josprox.kms.controller

import com.josprox.kms.model.Activation
import com.josprox.kms.repository.ActivationRepository
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1")
class ActivationApiController(private val activationRepository: ActivationRepository) {

    @PostMapping("/register-activation")
    fun register(
        @RequestParam machineName: String,
        @RequestParam ipAddress: String,
        @RequestParam softwareName: String
    ): Activation {
        val activation = Activation(
            machineName = machineName,
            ipAddress = ipAddress,
            softwareName = softwareName,
            activationDate = LocalDateTime.now(),
            expiryDate = LocalDateTime.now().plusDays(180)
        )
        return activationRepository.save(activation)
    }

    @GetMapping("/status")
    fun status() = mapOf("status" to "OK", "timestamp" to LocalDateTime.now())
}
