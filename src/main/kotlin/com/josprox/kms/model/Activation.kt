package com.josprox.kms.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "activations")
data class Activation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val machineName: String = "",

    @Column(nullable = false)
    val ipAddress: String = "",

    @Column(nullable = false)
    val softwareName: String = "",

    @Column(nullable = false)
    val activationDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val expiryDate: LocalDateTime = LocalDateTime.now().plusDays(180)
)
