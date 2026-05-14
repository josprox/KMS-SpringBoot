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

    private var tempIp = "Unknown"
    private var tempMachine = "Unknown"
    private var tempSoftware = "Windows"

    private fun parseAndSaveActivation(line: String) {
        val lowerLine = line.lowercase()
        
        // 1. Capture IP (though it might be 127.0.0.1 due to multiplexer)
        if (lowerLine.contains("connection accepted")) {
            val ipMatch = Regex("""(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})""").find(line)
            tempIp = ipMatch?.value ?: "Unknown"
        }

        // 2. Capture Software type
        if (lowerLine.contains("application id")) {
            tempSoftware = if (lowerLine.contains("office")) "Office" else "Windows"
        }

        // 3. Capture real Machine Name (the most important info)
        if (lowerLine.contains("workstation name")) {
            tempMachine = line.split(":").lastOrNull()?.trim() ?: "Unknown"
        }

        // 4. On response sent, persist to database
        if (lowerLine.contains("sending response")) {
            try {
                val activation = Activation(
                    ipAddress = tempIp,
                    machineName = tempMachine,
                    softwareName = tempSoftware,
                    activationDate = LocalDateTime.now(),
                    expiryDate = LocalDateTime.now().plusDays(180)
                )
                activationRepository.save(activation)
                logger.info(">>> DATABASE SUCCESS: Saved activation for $tempSoftware from $tempMachine")
                
                // Reset for next request
                tempMachine = "Unknown"
                tempIp = "Unknown"
            } catch (e: Exception) {
                logger.error("Failed to save to database: ${e.message}")
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
