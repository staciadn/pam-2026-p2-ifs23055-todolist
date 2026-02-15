package org.delcom

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureHTTP() {
    install(CORS) {
        // Izinkan metode standar yang digunakan dalam crud.test.js
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)    // Tambahkan GET secara eksplisit
        allowMethod(HttpMethod.Post)   // Tambahkan POST secara eksplisit
        allowMethod(HttpMethod.Put)    // Dibutuhkan untuk update data
        allowMethod(HttpMethod.Delete) // Dibutuhkan untuk hapus data
        allowMethod(HttpMethod.Patch)

        // Izinkan header yang dibutuhkan untuk pengiriman JSON
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        // Izinkan akses dari host mana pun (Penting untuk lingkungan pengembangan/testing)
        anyHost()
    }
}