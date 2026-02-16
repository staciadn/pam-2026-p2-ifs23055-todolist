package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable
data class CashFlowRequest(
    val type: String? = null,
    val source: String? = null,
    val label: String? = null,
    // UBAH INI: Gunakan String? agar controller bisa menangani validasi manual (force 500)
    val amount: String? = null,
    val description: String? = null
)