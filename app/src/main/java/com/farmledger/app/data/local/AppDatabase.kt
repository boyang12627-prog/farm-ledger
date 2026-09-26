package com.farmledger.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.farmledger.app.data.local.dao.AccountDao
import com.farmledger.app.data.local.dao.GameDayDao
import com.farmledger.app.data.local.dao.InventoryDao
import com.farmledger.app.data.local.dao.LedgerDao
import com.farmledger.app.data.local.dao.SettlementDao
import com.farmledger.app.data.local.entity.DailySettlementEntity
import com.farmledger.app.data.local.entity.GameDayStateEntity
import com.farmledger.app.data.local.entity.InventoryItemEntity
import com.farmledger.app.data.local.entity.LedgerAccountEntity
import com.farmledger.app.data.local.entity.LedgerEntryEntity

@Database(
    entities = [
        LedgerEntryEntity::class,
        LedgerAccountEntity::class,
        DailySettlementEntity::class,
        GameDayStateEntity::class,
        InventoryItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao
    abstract fun accountDao(): AccountDao
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

        /** M5：真港幣帳加 category／account／transfer；帳戶表 stub */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ledger_accounts (
                        id TEXT NOT NULL PRIMARY KEY,
                        nameZh TEXT NOT NULL,
                        archived INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO ledger_accounts (id, nameZh, archived)
                    VALUES ('cash', '現金', 0)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ledger_entries_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        localDate TEXT NOT NULL,
                        type TEXT NOT NULL,
                        amountMinor INTEGER NOT NULL,
                        note TEXT NOT NULL,
                        category TEXT,
                        accountId TEXT NOT NULL,
                        transferAccountId TEXT,
                        status TEXT NOT NULL,
                        createdAtEpochMs INTEGER NOT NULL,
                        updatedAtEpochMs INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO ledger_entries_new
                    (id, localDate, type, amountMinor, note, category, accountId, transferAccountId, status, createdAtEpochMs, updatedAtEpochMs)
                    SELECT id, localDate, type, amountMinor, note, NULL, 'cash', NULL, status, createdAtEpochMs, updatedAtEpochMs
                    FROM ledger_entries
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE ledger_entries")
                db.execSQL("ALTER TABLE ledger_entries_new RENAME TO ledger_entries")
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "farm_ledger.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
    }
}
