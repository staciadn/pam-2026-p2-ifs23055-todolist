package org.delcom.services

import org.delcom.data.CashFlowQuery
import org.delcom.data.CashFlowRequest
import org.delcom.entities.CashFlow
import org.delcom.helpers.loadInitialData
import org.delcom.repositories.ICashFlowRepository
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class CashFlowService(private val repository: ICashFlowRepository) : ICashFlowService {

    // ... (getAllCashFlows tetap sama) ...
    override fun getAllCashFlows(query: CashFlowQuery): List<CashFlow> {
        // OPTIMASI: Pindahkan formatter ke LUAR filter agar tidak dibuat ulang jutaan kali
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        // OPTIMASI: Parse tanggal query di LUAR filter (hanya 1x jalan)
        val startLimit = query.startDate?.let { try { LocalDate.parse(it, dateFormatter) } catch (e: Exception) { null } }
        val endLimit = query.endDate?.let { try { LocalDate.parse(it, dateFormatter) } catch (e: Exception) { null } }

        // OPTIMASI: Pecah labels query di LUAR filter
        val searchTags = query.labels?.split(",")?.map { it.trim().lowercase() }?.filter { it.isNotBlank() }

        return repository.getAll().filter { cf ->
            // Gunakan Short-circuit (&&) agar jika satu salah, yang bawah tidak diproses

            val matchType = query.type == null || cf.type.equals(query.type, true)
            if (!matchType) return@filter false

            val matchSource = query.source == null || cf.source.equals(query.source, true)
            if (!matchSource) return@filter false

            val matchGte = query.gteAmount == null || cf.amount >= query.gteAmount
            val matchLte = query.lteAmount == null || cf.amount <= query.lteAmount
            if (!matchGte || !matchLte) return@filter false

            val matchSearch = query.search == null || cf.description.contains(query.search, true)
            if (!matchSearch) return@filter false

            // Logika Label yang lebih ringan
            if (searchTags != null && searchTags.isNotEmpty()) {
                val itemLabels = cf.label.split(",").map { it.trim().lowercase() }
                if (!searchTags.any { tag -> itemLabels.contains(tag) }) return@filter false
            }

            // Filter Tanggal (Penyebab utama timeout)
            if (startLimit != null || endLimit != null) {
                val cfDate = try {
                    // substring(0,10) biasanya "YYYY-MM-DD"
                    LocalDate.parse(cf.createdAt.substring(0, 10))
                } catch (e: Exception) { null }

                if (cfDate == null) return@filter false
                if (startLimit != null && cfDate.isBefore(startLimit)) return@filter false
                if (endLimit != null && cfDate.isAfter(endLimit)) return@filter false
            }

            true
        }
    }

    override fun getCashFlowById(id: String) = repository.getById(id)

    // Implementasi Method Baru
    override fun createCashFlowRaw(type: String, source: String, label: String, amount: Double, description: String): String {
        val id = UUID.randomUUID().toString()
        val now = OffsetDateTime.now().toString()
        val newCf = CashFlow(id, type, source, label, amount, description, now, now)
        repository.add(newCf)
        return id
    }

    // Implementasi untuk backward compatibility (jika diperlukan)
    override fun createCashFlow(req: CashFlowRequest): String {
        return createCashFlowRaw(req.type!!, req.source!!, req.label!!, req.amount!!.toDouble(), req.description!!)
    }

    override fun updateCashFlowRaw(id: String, type: String, source: String, label: String, amount: Double, description: String): Boolean {
        val existing = repository.getById(id) ?: return false
        val updated = existing.copy(
            type = type, source = source, label = label, amount = amount, description = description,
            updatedAt = OffsetDateTime.now().toString()
        )
        return repository.update(id, updated)
    }

    override fun deleteCashFlow(id: String) = repository.delete(id)

    override fun setupInitialData(): Int {
        repository.clearAll()
        val data = loadInitialData()
        data.forEach { repository.add(it) }
        return data.size
    }

    override fun getDistinctTypes() = repository.getAll().map { it.type }.distinct()
    override fun getDistinctSources() = repository.getAll().map { it.source }.distinct()
    override fun getDistinctLabels() = repository.getAll().flatMap { it.label.split(",") }.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
}