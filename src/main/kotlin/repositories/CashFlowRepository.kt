package org.delcom.repositories

import org.delcom.entities.CashFlow
import java.util.Collections

class CashFlowRepository : ICashFlowRepository {
    // Gunakan LinkedHashMap: Pencarian ID instan (O(1)), urutan tetap terjaga.
    // Collections.synchronizedMap: Mencegah error saat ribuan tes jalan bersamaan.
    private val cashFlows = Collections.synchronizedMap(LinkedHashMap<String, CashFlow>())

    override fun getAll(): List<CashFlow> = cashFlows.values.toList()

    override fun getById(id: String): CashFlow? = cashFlows[id]

    override fun add(cf: CashFlow) {
        cashFlows[cf.id] = cf
    }

    override fun update(id: String, cf: CashFlow): Boolean {
        if (!cashFlows.containsKey(id)) return false
        cashFlows[id] = cf
        return true
    }

    override fun delete(id: String): Boolean = cashFlows.remove(id) != null

    override fun clearAll() {
        cashFlows.clear()
    }
}