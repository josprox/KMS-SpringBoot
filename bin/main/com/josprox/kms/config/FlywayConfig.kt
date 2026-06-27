package com.josprox.kms.config

import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource
import org.slf4j.LoggerFactory

@Configuration
class FlywayConfig(private val dataSource: DataSource) {
    private val logger = LoggerFactory.getLogger(FlywayConfig::class.java)

    @Bean(initMethod = "migrate")
    fun flyway(): Flyway {
        logger.info("Manual Flyway migration starting for schema 'public'...")
        return Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .locations("classpath:db/migration")
            .schemas("public")
            .load()
    }
}
