package org.delcom.repositories

import org.delcom.entities.CashFlow

interface ICashFlowRepository {
    fun getAll(): List<CashFlow>
    fun getById(id: String): CashFlow?
    fun add(cashFlow: CashFlow)
    fun update(id: String, cashFlow: CashFlow): Boolean
    fun delete(id: String): Boolean
    fun clearAll()
}