package org.delcom.repositories

import org.delcom.entities.CashFlow

class CashFlowRepository : ICashFlowRepository {
    private val cashFlows = mutableListOf<CashFlow>()

    override fun getAll(): List<CashFlow> = cashFlows

    override fun getById(id: String): CashFlow? = cashFlows.find { it.id == id }

    override fun add(cashFlow: CashFlow) {
        cashFlows.add(cashFlow)
    }

    override fun update(id: String, cashFlow: CashFlow): Boolean {
        val index = cashFlows.indexOfFirst { it.id == id }
        if (index != -1) {
            cashFlows[index] = cashFlow
            return true
        }
        return false
    }

    override fun delete(id: String): Boolean {
        return cashFlows.removeIf { it.id == id }
    }

    override fun clearAll() {
        cashFlows.clear()
    }
}