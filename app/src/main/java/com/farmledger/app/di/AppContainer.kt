package com.farmledger.app.di

import android.content.Context
import com.farmledger.app.data.local.AppDatabase
import com.farmledger.app.data.local.datastore.GamePreferences
import com.farmledger.app.data.repository.FarmLedgerRepository

class AppContainer(context: Context) {
    private val db = AppDatabase.get(context)
    private val prefs = GamePreferences(context)
    val repository = FarmLedgerRepository(
        ledgerDao = db.ledgerDao(),
        settlementDao = db.settlementDao(),
        prefs = prefs
    )
}
