package com.josprox.kms.repository

import com.josprox.kms.model.Activation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ActivationRepository : JpaRepository<Activation, Long>

@Repository
interface AdminUserRepository : org.springframework.data.jpa.repository.JpaRepository<com.josprox.kms.model.AdminUser, Long> {
    fun findByUsername(username: String): com.josprox.kms.model.AdminUser?
}
