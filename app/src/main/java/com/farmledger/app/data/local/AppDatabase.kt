package com.farmledger.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.farmledger.app.data.local.dao.GameDayDao
import com.farmledger.app.data.local.dao.InventoryDao
import com.farmledger.app.data.local.dao.LedgerDao
import com.farmledger.app.data.local.dao.SettlementDao
import com.farmledger.app.data.local.entity.DailySettlementEntity
import com.farmledger.app.data.local.entity.GameDayStateEntity
import com.farmledger.app.data.local.entity.InventoryItemEntity
import com.farmledger.app.data.local.entity.LedgerEntryEntity

@Database(
    entities = [
        LedgerEntryEntity::class,
        DailySettlementEntity::class,
        GameDayStateEntity::class,
        InventoryItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao
    abstract fun settlementDao(): SettlementDao
    abstract fun gameDayDao(): GameDayDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS game_day_state (
                        id INTEGER NOT NULL PRIMARY KEY,
                        gameDay INTEGER NOT NULL,
                        phase TEXT NOT NULL,
                        farmStageHint TEXT NOT NULL,
                        lastWallClockEpochMs INTEGER NOT NULL,
                        updatedAtEpochMs INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO game_day_state
                    (id, gameDay, phase, farmStageHint, lastWallClockEpochMs, updatedAtEpochMs)
                    VALUES (1, 1, 'MORNING', 'BARREN', 0, 0)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS inventory_items (
                        id TEXT NOT NULL PRIMARY KEY,
                        kind TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        updatedAtEpochMs INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "farm_ledger.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
