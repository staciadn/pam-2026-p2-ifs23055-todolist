//package org.delcom
//
//import io.ktor.http.*
//import io.ktor.server.application.*
//import io.ktor.server.plugins.statuspages.*
//import io.ktor.server.response.*
//
//fun Application.configureStatusPages() {
//    install(StatusPages) {
//        // Menangkap semua error yang tidak terduga (misal: NumberFormatException)
//        // Tes mengharapkan status 500 dengan body { status: "error", ... }
//        exception<Throwable> { call, cause ->
//            call.respond(HttpStatusCode.InternalServerError, DataResponse(
//                status = "error",
//                message = cause.message ?: "Internal Server Error",
//                data = null
//            ))
//        }
//    }
//}