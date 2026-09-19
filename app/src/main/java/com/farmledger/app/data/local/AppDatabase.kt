package com.farmledger.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.farmledger.app.data.local.dao.LedgerDao
import com.farmledger.app.data.local.dao.SettlementDao
import com.farmledger.app.data.local.entity.DailySettlementEntity
import com.farmledger.app.data.local.entity.LedgerEntryEntity

@Database(
    entities = [LedgerEntryEntity::class, DailySettlementEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao
    abstract fun settlementDao(): SettlementDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "farm_ledger.db"
                ).build().also { instance = it }
            }
    }
}
