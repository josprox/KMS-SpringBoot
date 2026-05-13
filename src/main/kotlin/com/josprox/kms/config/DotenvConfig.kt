package com.josprox.kms.config

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.context.annotation.Configuration
import jakarta.annotation.PostConstruct

@Configuration
class DotenvConfig {

    @PostConstruct
    fun init() {
        val dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load()
        
        dotenv.entries().forEach { entry ->
            System.setProperty(entry.key, entry.value)
        }
    }
}
