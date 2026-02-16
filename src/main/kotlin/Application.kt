package org.delcom

import io.ktor.server.application.*
import org.delcom.data.appModule // PENTING: Gunakan appModule dari package data
import org.koin.ktor.plugin.Koin
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import io.ktor.server.netty.EngineMain
import io.github.cdimascio.dotenv.dotenv

fun main(args: Array<String>) {
    val dotenv = dotenv { directory = "."; ignoreIfMissing = false }
    dotenv.entries().forEach { System.setProperty(it.key, it.value) }
    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true; ignoreUnknownKeys = true; isLenient = true })
    }
    install(Koin) { modules(appModule) }
    configureRouting()
}