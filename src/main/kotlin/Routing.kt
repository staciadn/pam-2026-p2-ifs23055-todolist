package org.delcom

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.delcom.controllers.CashFlowController
import org.delcom.data.ErrorResponse
import org.delcom.data.AppException
import org.delcom.helpers.parseMessageToMap
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val controller: CashFlowController by inject()

    install(StatusPages) {
        exception<AppException> { call, cause ->
            val dataMap = parseMessageToMap(cause.message)
            call.respond(
                status = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data = dataMap
                )
            )
        }
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(status = "error", message = cause.message ?: "Internal Server Error")
            )
        }
    }

    routing {
        get("/") { call.respondText("11S23055") }

        route("/cash-flows") {
            post("/setup") { controller.setupData(call) }
            get("/types") { controller.getTypes(call) }
            get("/sources") { controller.getSources(call) }
            get("/labels") { controller.getLabels(call) }
            get { controller.getAll(call) }
            post { controller.create(call) }
            get("/{id}") { controller.getById(call) }
            put("/{id}") { controller.update(call) }
            delete("/{id}") { controller.delete(call) }
        }
    }
}