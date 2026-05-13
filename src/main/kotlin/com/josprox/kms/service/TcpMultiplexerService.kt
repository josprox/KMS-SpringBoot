package com.josprox.kms.service

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

@Service
class TcpMultiplexerService {
    private val logger = LoggerFactory.getLogger(TcpMultiplexerService::class.java)
    private val executor = Executors.newCachedThreadPool()
    private var serverSocket: ServerSocket? = null

    @Value("\${kms.public-port:8080}")
    private var publicPort: Int = 8080

    @Value("\${server.port:8081}")
    private var webPort: Int = 8081

    @Value("\${kms.internal-kms-port:1688}")
    private var kmsPort: Int = 1688

    @PostConstruct
    fun startMultiplexer() {
        Thread {
            try {
                serverSocket = ServerSocket(publicPort)
                logger.info("TCP Multiplexer started on port $publicPort")
                logger.info("Forwarding HTTP to $webPort and KMS to $kmsPort")

                while (!serverSocket!!.isClosed) {
                    val clientSocket = serverSocket!!.accept()
                    executor.execute { handleConnection(clientSocket) }
                }
            } catch (e: Exception) {
                if (!serverSocket!!.isClosed) {
                    logger.error("Multiplexer error: ${e.message}")
                }
            }
        }.apply {
            isDaemon = true
            name = "KMS-Multiplexer"
            start()
        }
    }

    private fun handleConnection(clientSocket: Socket) {
        try {
            val inputStream = clientSocket.getInputStream()
            val peekBuffer = ByteArray(4)
            val bytesRead = inputStream.read(peekBuffer)

            if (bytesRead <= 0) {
                clientSocket.close()
                return
            }

            val firstBytes = String(peekBuffer, 0, bytesRead)
            // HTTP methods usually start with these letters: G(ET), P(OST), H(EAD), O(PTIONS), D(ELETE), C(ONNECT), T(RACE), P(ATCH)
            val isHttp = firstBytes.startsWith("GET") || 
                         firstBytes.startsWith("POS") || 
                         firstBytes.startsWith("HEA") || 
                         firstBytes.startsWith("PUT") ||
                         firstBytes.startsWith("DEL")

            val targetPort = if (isHttp) webPort else kmsPort
            
            val targetSocket = Socket("localhost", targetPort)
            
            // Send the peeked bytes to the target first
            targetSocket.getOutputStream().write(peekBuffer, 0, bytesRead)
            
            // Start bi-directional copy
            executor.execute { bridge(clientSocket.getInputStream(), targetSocket.getOutputStream()) }
            bridge(targetSocket.getInputStream(), clientSocket.getOutputStream())
            
        } catch (e: Exception) {
            // Quietly close on errors
            try { clientSocket.close() } catch (ex: Exception) {}
        }
    }

    private fun bridge(input: InputStream, output: OutputStream) {
        try {
            val buffer = ByteArray(8192)
            var length: Int
            while (input.read(buffer).also { length = it } != -1) {
                output.write(buffer, 0, length)
                output.flush()
            }
        } catch (e: Exception) {
            // Connection closed
        } finally {
            try { input.close() } catch (ex: Exception) {}
            try { output.close() } catch (ex: Exception) {}
        }
    }

    @PreDestroy
    fun stopMultiplexer() {
        logger.info("Stopping TCP Multiplexer...")
        serverSocket?.close()
        executor.shutdownNow()
    }
}
