package com.josprox.kms

import com.josprox.kms.model.AdminUser
import com.josprox.kms.repository.AdminUserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@SpringBootApplication
class KmsApplication

fun main(args: Array<String>) {
    runApplication<KmsApplication>(*args)
}

@Component
class DataInitializer(
    private val adminUserRepository: AdminUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val environment: Environment
) : CommandLineRunner {

    override fun run(vararg args: String) {
        val u = (environment.getProperty("ADMIN_USERNAME") ?: "admin").toString()
        val p = (environment.getProperty("ADMIN_PASSWORD") ?: "admin123").toString()
        
        val admin = adminUserRepository.findByUsername(u)
        if (admin == null) {
            val newAdmin = AdminUser(
                username = u,
                passwordHash = passwordEncoder.encode(p).toString(),
                role = "ROLE_ADMIN"
            )
            adminUserRepository.save(newAdmin)
            println("Initial admin user created.")
        } else {
            val updatedAdmin = admin.copy(
                passwordHash = passwordEncoder.encode(p).toString()
            )
            adminUserRepository.save(updatedAdmin)
            println("Admin user verified.")
        }
    }
}
