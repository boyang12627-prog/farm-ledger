package com.farmledger.app

import android.app.Application
import com.farmledger.app.di.AppContainer

class FarmLedgerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
