package com.josprox.kms.service

import com.josprox.kms.model.Activation
import com.josprox.kms.repository.ActivationRepository
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class KmsEmulatorService(private val activationRepository: ActivationRepository) {
    private val logger = LoggerFactory.getLogger(KmsEmulatorService::class.java)
    private var process: Process? = null

    @Value("\${kms.vlmcsd.path:/usr/local/bin/vlmcsd}")
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
            // -D: Run in foreground, -d: log to stdout, -t: timeout, -e: log EPID, -v: verbose
            val processBuilder = ProcessBuilder(vlmcsdPath, "-D", "-d", "-t", "3", "-e", "-v")
            processBuilder.redirectErrorStream(true)
            process = processBuilder.start()
            
            Thread {
                process?.inputStream?.bufferedReader()?.use { reader ->
                    reader.forEachLine { line ->
                        logger.info("[KMS-EMULATOR] $line")
                        parseAndSaveActivation(line)
                    }
                }
            }.start()
            
            logger.info("vlmcsd emulator started successfully.")
        } catch (e: Exception) {
            logger.error("Failed to start vlmcsd emulator: ${e.message}", e)
        }
    }

    private fun parseAndSaveActivation(line: String) {
        // Example: 2026-05-13 04:05:08: KMS request from 192.168.1.1 for Windows 10 Pro
        // Note: Actual vlmcsd output varies, this is a heuristic approach
        if (line.contains("KMS request from") || line.contains("RPC request:")) {
            try {
                val ipMatch = Regex("""(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})""").find(line)
                val ip = ipMatch?.value ?: "Unknown"
                
                var software = "Windows/Office"
                if (line.contains("Windows", ignoreCase = true)) software = "Windows"
                if (line.contains("Office", ignoreCase = true)) software = "Office"

                val activation = Activation(
                    ipAddress = ip,
                    machineName = "Remote Client",
                    softwareName = software,
                    activationDate = LocalDateTime.now(),
                    expiryDate = LocalDateTime.now().plusDays(180)
                )
                activationRepository.save(activation)
                logger.info("New activation recorded: $software from $ip")
            } catch (e: Exception) {
                logger.warn("Failed to parse activation line: $line")
            }
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
