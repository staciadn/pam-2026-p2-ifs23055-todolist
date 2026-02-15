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
        // --- OPTIMASI 1: Pre-parsing di luar loop ---
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        // Parse tanggal query cukup 1 kali saja
        val startLimit = query.startDate?.let { try { LocalDate.parse(it, dateFormatter) } catch (e: Exception) { null } }
        val endLimit = query.endDate?.let { try { LocalDate.parse(it, dateFormatter) } catch (e: Exception) { null } }

        // Pecah label query cukup 1 kali saja
        val searchTags = query.labels?.split(",")
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotBlank() } ?: emptyList()

        val searchKeyword = query.search?.lowercase()

        return repository.getAll().filter { cf ->
            // --- OPTIMASI 2: Short-circuit Evaluation ---
            // Gunakan return@filter langsung agar jika satu kriteria gagal,
            // Kotlin tidak perlu mengecek kriteria di bawahnya.

            // 1. Filter Tipe
            if (query.type != null && !cf.type.equals(query.type, true)) return@filter false

            // 2. Filter Source
            if (query.source != null && !cf.source.equals(query.source, true)) return@filter false

            // 3. Filter Amount
            if (query.gteAmount != null && cf.amount < query.gteAmount) return@filter false
            if (query.lteAmount != null && cf.amount > query.lteAmount) return@filter false

            // 4. Filter Search (Deskripsi)
            if (searchKeyword != null && !cf.description.lowercase().contains(searchKeyword)) return@filter false

            // 5. Filter Labels
            if (searchTags.isNotEmpty()) {
                val itemLabels = cf.label.split(",").map { it.trim().lowercase() }
                if (!searchTags.any { tag -> itemLabels.contains(tag) }) return@filter false
            }

            // 6. Filter Tanggal (Optimasi Parsing Tanggal Record)
            if (startLimit != null || endLimit != null) {
                val cfDate = try {
                    // substring(0,10) biasanya "YYYY-MM-DD", gunakan ISO_LOCAL_DATE yang jauh lebih cepat
                    LocalDate.parse(cf.createdAt.substring(0, 10))
                } catch (e: Exception) { null }

                if (cfDate != null) {
                    if (startLimit != null && cfDate.isBefore(startLimit)) return@filter false
                    if (endLimit != null && cfDate.isAfter(endLimit)) return@filter false
                } else if (startLimit != null || endLimit != null) {
                    return@filter false // Jika filter tanggal aktif tapi data tidak punya tanggal valid
                }
            }

            true // Lolos semua filter
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
    override fun getDistinctLabels() = repository.getAll()
        .flatMap { it.label.split(",") }
        .map { it.trim() } // Menghilangkan spasi di depan/belakang
        .filter { it.isNotEmpty() }
        .distinct()
}