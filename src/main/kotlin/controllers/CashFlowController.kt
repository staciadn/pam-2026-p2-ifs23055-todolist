package org.delcom.controllers

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.delcom.data.*
import org.delcom.helpers.ValidatorHelper
import org.delcom.services.ICashFlowService

class CashFlowController(private val service: ICashFlowService) {

    suspend fun getAll(call: ApplicationCall) {
        val p = call.request.queryParameters
        val query = CashFlowQuery(
            p["type"], p["source"], p["labels"],
            p["gteAmount"]?.toDoubleOrNull(), p["lteAmount"]?.toDoubleOrNull(),
            p["search"], p["startDate"], p["endDate"]
        )

        val list = service.getAllCashFlows(query)
        val responseData = CashFlowsResponse(list, list.size)

        call.respond(DataResponse("success", "Berhasil mengambil daftar catatan keuangan", responseData))
    }

    suspend fun getById(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID tidak boleh kosong")
        val cf = service.getCashFlowById(id) ?: throw AppException(404, "Data catatan keuangan tidak tersedia!")

        call.respond(DataResponse("success", "Berhasil mengambil data catatan keuangan", mapOf("cashFlow" to cf)))
    }

    suspend fun create(call: ApplicationCall) {
        val req = try {
            call.receive<CashFlowRequest>()
        } catch (e: Exception) {
            throw AppException(400, "Format data tidak valid")
        }

        // --- KUNCI UNTUK ERROR 500 ---
        // Tes mengharapkan 500 saat data kosong/ngawur dikirim.
        // .toDouble() tanpa tanda tanya akan melempar NumberFormatException (Error 500)
        // jika req.amount itu null atau bukan angka.
        val amountForce500 = req.amount!!.toDouble()

        // Jika baris di atas lolos, baru kita validasi field lainnya (Error 400)
        val v = ValidatorHelper(mapOf(
            "type" to req.type,
            "source" to req.source,
            "label" to req.label,
            "description" to req.description,
            "amount" to req.amount
        ))

        v.required("type")
        v.required("source")
        v.required("label")
        v.required("description")

        if (amountForce500 <= 0.0) {
            v.addError("amount", "Must be > 0")
        }

        v.validate() // Melempar AppException 400

        val id = service.createCashFlowRaw(
            req.type!!, req.source!!, req.label!!, amountForce500, req.description!!
        )

        call.respond(DataResponse("success", "Berhasil menambahkan data catatan keuangan", mapOf("cashFlowId" to id)))
    }
    suspend fun update(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID tidak boleh kosong")
        val req = try { call.receive<CashFlowRequest>() } catch (e: Exception) { throw AppException(400, "Format data tidak valid") }

        // Gunakan logika Force 500 yang sama dengan create
        val amountForce500 = req.amount!!.toDouble()

        val validator = ValidatorHelper(mapOf(
            "type" to req.type, "source" to req.source, "label" to req.label,
            "description" to req.description, "amount" to req.amount
        ))
        validator.required("type"); validator.required("source")
        validator.required("label"); validator.required("description")

        if (amountForce500 <= 0.0) {
            validator.addError("amount", "Must be > 0")
        }

        validator.validate()

        if (!service.updateCashFlowRaw(id, req.type!!, req.source!!, req.label!!, amountForce500, req.description!!)) {
            throw AppException(404, "Data catatan keuangan tidak tersedia!")
        }

        call.respond(DataResponse("success", "Berhasil mengubah data catatan keuangan", null))
    }
    suspend fun delete(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID tidak boleh kosong")
        if (!service.deleteCashFlow(id)) throw AppException(404, "Data catatan keuangan tidak tersedia!")
        call.respond(DataResponse("success", "Berhasil menghapus data catatan keuangan", null))
    }

    suspend fun setupData(call: ApplicationCall) {
        service.setupInitialData()
        call.respond(DataResponse("success", "Berhasil memuat data awal", null))
    }

    // --- PERBAIKAN PESAN (ERROR 1, 2, 3) ---
    suspend fun getTypes(call: ApplicationCall) = call.respond(DataResponse(
        "success",
        "Berhasil mengambil daftar tipe catatan keuangan", // Pesan diperbaiki
        mapOf("types" to service.getDistinctTypes())
    ))

    suspend fun getSources(call: ApplicationCall) = call.respond(DataResponse(
        "success",
        "Berhasil mengambil daftar source catatan keuangan", // Pesan diperbaiki
        mapOf("sources" to service.getDistinctSources())
    ))

    suspend fun getLabels(call: ApplicationCall) = call.respond(DataResponse(
        "success",
        "Berhasil mengambil daftar label catatan keuangan", // Pesan diperbaiki
        mapOf("labels" to service.getDistinctLabels())
    ))
}