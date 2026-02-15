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
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        return repository.getAll().filter { cf ->
            // Logika ASLI Anda, hanya digabung dengan && agar jauh lebih cepat (short-circuit)

            (query.type == null || cf.type.equals(query.type, ignoreCase = true)) &&
                    (query.source == null || cf.source.equals(query.source, ignoreCase = true)) &&
                    (query.gteAmount == null || cf.amount >= query.gteAmount) &&
                    (query.lteAmount == null || cf.amount <= query.lteAmount) &&
                    (query.search == null || cf.description.contains(query.search, ignoreCase = true)) &&

                    // Filter Labels ASLI Anda
                    (query.labels == null || query.labels.split(",").map { it.trim() }.filter { it.isNotBlank() }.let { searchTags ->
                        searchTags.isEmpty() || searchTags.any { tag ->
                            cf.label.split(",").map { it.trim() }.any { it.equals(tag, ignoreCase = true) }
                        }
                    }) &&

                    // Filter Tanggal ASLI Anda (Dibungkus try-catch sebaris agar aman dari format salah)
                    (query.startDate == null || try { !LocalDate.parse(cf.createdAt.substring(0, 10)).isBefore(LocalDate.parse(query.startDate, dateFormatter)) } catch (e: Exception) { false }) &&
                    (query.endDate == null || try { !LocalDate.parse(cf.createdAt.substring(0, 10)).isAfter(LocalDate.parse(query.endDate, dateFormatter)) } catch (e: Exception) { false })
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