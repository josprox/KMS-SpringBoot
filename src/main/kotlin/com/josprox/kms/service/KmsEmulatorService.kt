package com.josprox.kms.service

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.TimeUnit

@Service
class KmsEmulatorService {
    private val logger = LoggerFactory.getLogger(KmsEmulatorService::class.java)
    private var process: Process? = null

    @Value("\${kms.vlmcsd.path:/vlmcsd}")
    private lateinit var vlmcsdPath: String

    @PostConstruct
    fun startEmulator() {
        val file = File(vlmcsdPath)
        if (!file.exists()) {
            logger.warn("vlmcsd binary not found at $vlmcsdPath. Skipping emulator startup.")
            return
        }

        try {
            logger.info("Starting vlmcsd emulator from $vlmcsdPath...")
            val processBuilder = ProcessBuilder(vlmcsdPath, "-D", "-d", "-t", "3", "-e", "-v")
            processBuilder.redirectErrorStream(true)
            process = processBuilder.start()
            
            // Read output in a separate thread to avoid blocking
            Thread {
                process?.inputStream?.bufferedReader()?.use { reader ->
                    reader.forEachLine { line ->
                        logger.info("[KMS-EMULATOR] $line")
                    }
                }
            }.start()
            
            logger.info("vlmcsd emulator started successfully.")
        } catch (e: Exception) {
            logger.error("Failed to start vlmcsd emulator: ${e.message}", e)
        }
    }

    @PreDestroy
    fun stopEmulator() {
        logger.info("Stopping vlmcsd emulator...")
        process?.let {
            it.destroy()
            if (!it.waitFor(5, TimeUnit.SECONDS)) {
                it.destroyForcibly()
            }
            logger.info("vlmcsd emulator stopped.")
        }
    }
}
