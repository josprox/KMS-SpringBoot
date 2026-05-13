package com.josprox.kms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KmsApplication

fun main(args: Array<String>) {
    runApplication<KmsApplication>(*args)
}
