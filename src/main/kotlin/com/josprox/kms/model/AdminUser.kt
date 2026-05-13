package com.josprox.kms.model

import jakarta.persistence.*

@Entity
@Table(name = "admin_users")
data class AdminUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    val passwordHash: String,

    @Column(nullable = false)
    val role: String = "ROLE_ADMIN"
)
