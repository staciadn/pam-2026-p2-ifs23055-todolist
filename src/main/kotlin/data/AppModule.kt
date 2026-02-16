package org.delcom.data

import org.koin.dsl.module
// --- TAMBAHKAN IMPORT INI ---
import org.delcom.repositories.CashFlowRepository
import org.delcom.repositories.ICashFlowRepository
import org.delcom.services.CashFlowService
import org.delcom.services.ICashFlowService
import org.delcom.controllers.CashFlowController
// ----------------------------

val appModule = module {
    // Repository
    single<ICashFlowRepository> { CashFlowRepository() }

    // Service
    single<ICashFlowService> { CashFlowService(get()) }

    // Controller
    single { CashFlowController(get()) }
}