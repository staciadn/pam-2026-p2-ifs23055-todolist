package org.delcom.data

import kotlinx.serialization.Serializable
// --- TAMBAHKAN IMPORT INI ---
import org.delcom.entities.CashFlow
// ----------------------------

@Serializable
data class CashFlowsResponse(
    val cashFlows: List<CashFlow>,
    val total: Int
)