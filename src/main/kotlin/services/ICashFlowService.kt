package org.delcom.services

import org.delcom.data.CashFlowQuery
import org.delcom.data.CashFlowRequest
import org.delcom.entities.CashFlow

interface ICashFlowService {
    fun getAllCashFlows(query: CashFlowQuery): List<CashFlow>
    fun getCashFlowById(id: String): CashFlow?

    // Function lama (bisa dihapus kalau tidak dipakai, atau biarkan)
    fun createCashFlow(req: CashFlowRequest): String

    // Function BARU untuk handle data yang sudah diparse controller
    fun createCashFlowRaw(type: String, source: String, label: String, amount: Double, description: String): String
    fun updateCashFlowRaw(id: String, type: String, source: String, label: String, amount: Double, description: String): Boolean

    fun deleteCashFlow(id: String): Boolean
    fun setupInitialData(): Int

    fun getDistinctTypes(): List<String>
    fun getDistinctSources(): List<String>
    fun getDistinctLabels(): List<String>
}