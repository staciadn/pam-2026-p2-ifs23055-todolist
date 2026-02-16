package org.delcom

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            // Mengabaikan properti yang tidak dikenal di JSON agar tidak error
            ignoreUnknownKeys = true
            // Memastikan output JSON rapi (opsional, membantu saat debugging)
            prettyPrint = true
            // Memastikan nilai default tetap disertakan dalam serialisasi
            encodeDefaults = true
        })
    }
}