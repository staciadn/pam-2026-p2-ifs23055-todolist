package org.delcom.controllers

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.delcom.data.AppException
import org.delcom.data.CashFlowQuery
import org.delcom.data.CashFlowRequest
import org.delcom.data.CashFlowResponse
import org.delcom.data.DataResponse
import org.delcom.helpers.ValidatorHelper
import org.delcom.helpers.loadInitialData
import org.delcom.services.ICashFlowService
import kotlin.time.* // Menggunakan Instant dan Clock dari sini

class CashFlowController(private val cashFlowService: ICashFlowService) {

    suspend fun setupData(call: ApplicationCall) {
        val query = CashFlowQuery()
        val cashFlows = cashFlowService.getAllCashFlows(query)

        // Bersihkan data lama
        for (cashFlow in cashFlows) {
            cashFlowService.removeCashFlow(cashFlow.id)
        }

        val initCashFlows = loadInitialData()

        for (cashFlow in initCashFlows) {
            cashFlowService.createRawCashFlow(
                cashFlow.id,
                cashFlow.type,
                cashFlow.source,
                cashFlow.label,
                cashFlow.amount,
                cashFlow.description,
                cashFlow.createdAt, // Pastikan ini kotlin.time.Instant
                cashFlow.updatedAt  // Pastikan ini kotlin.time.Instant
            )
        }

        call.respond(DataResponse("success", "Berhasil memuat data awal", null))
    }

    suspend fun getAllCashFlows(call: ApplicationCall) {
        val queryParams = call.request.queryParameters
        val query = CashFlowQuery(
            type = queryParams["type"],
            source = queryParams["source"],
            labels = queryParams["labels"],
            gteAmount = queryParams["gteAmount"]?.toLongOrNull(),
            lteAmount = queryParams["lteAmount"]?.toLongOrNull(),
            search = queryParams["search"],
            startDate = queryParams["startDate"],
            endDate = queryParams["endDate"]
        )

        val cashFlows = cashFlowService.getAllCashFlows(query)
        call.respond(DataResponse("success", "Berhasil mengambil daftar catatan keuangan", mapOf("cashFlows" to cashFlows)))
    }

    suspend fun getCashFlowById(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID tidak boleh kosong!")
        val cashFlow = cashFlowService.getCashFlowById(id) ?: throw AppException(404, "Data tidak tersedia!")

        call.respond(DataResponse("success", "Berhasil mengambil data cash-flow", mapOf("cashFlow" to cashFlow)))
    }

    suspend fun createCashFlow(call: ApplicationCall) {
        val request = call.receive<CashFlowRequest>()

        val requestData = mapOf(
            "type" to request.type,
            "source" to request.source,
            "label" to request.label,
            "amount" to request.amount,
            "description" to request.description
        )

        val validatorHelper = ValidatorHelper(requestData)
        validatorHelper.required("type", "Tipe tidak boleh kosong")
        validatorHelper.required("source", "Sumber tidak boleh kosong")
        validatorHelper.required("amount", "Jumlah tidak boleh kosong")
        // Pastikan Anda sudah menambahkan fungsi minAmount di ValidatorHelper (lihat poin 2 di bawah)
        validatorHelper.minAmount("amount", 1, "Jumlah harus lebih besar dari 0")
        validatorHelper.validate()

        val newId = cashFlowService.createCashFlow(
            request.type!!,
            request.source!!,
            request.label ?: "",
            request.amount!!,
            request.description ?: ""
        )

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data catatan keuangan",
            CashFlowResponse(newId)
        )
        call.respond(response)
    }

    suspend fun updateCashFlow(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID tidak boleh kosong!")
        val request = call.receive<CashFlowRequest>()

        val requestData = mapOf(
            "type" to request.type,
            "source" to request.source,
            "amount" to request.amount
        )

        val validatorHelper = ValidatorHelper(requestData)
        validatorHelper.required("type", "Tipe tidak boleh kosong")
        validatorHelper.required("amount", "Jumlah tidak boleh kosong")
        validatorHelper.validate()

        val isUpdated = cashFlowService.updateCashFlow(
            id,
            request.type!!,
            request.source ?: "",
            request.label ?: "",
            request.amount!!,
            request.description ?: ""
        )

        if (!isUpdated) throw AppException(404, "Data tidak tersedia!")

        call.respond(DataResponse("success", "Berhasil mengubah data", null))
    }

    suspend fun deleteCashFlow(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw AppException(400, "ID tidak boleh kosong!")

        if (!cashFlowService.removeCashFlow(id)) {
            throw AppException(404, "Data tidak tersedia!")
        }

        call.respond(DataResponse("success", "Berhasil menghapus data", null))
    }

    suspend fun getTypes(call: ApplicationCall) {
        val types = cashFlowService.getTypes()
        call.respond(DataResponse("success", "Berhasil mengambil daftar tipe catatan keuangan", mapOf("types" to types)))
    }

    suspend fun getSources(call: ApplicationCall) {
        val sources = cashFlowService.getSources()
        call.respond(DataResponse("success", "Berhasil mengambil daftar source catatan keuangan", mapOf("sources" to sources)))
    }

    suspend fun getLabels(call: ApplicationCall) {
        val labels = cashFlowService.getLabels()
        call.respond(DataResponse("success", "Berhasil mengambil daftar label catatan keuangan", mapOf("labels" to labels)))
    }
}