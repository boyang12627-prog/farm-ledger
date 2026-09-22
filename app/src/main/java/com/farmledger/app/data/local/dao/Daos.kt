package com.farmledger.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.farmledger.app.data.local.entity.DailySettlementEntity
import com.farmledger.app.data.local.entity.GameDayStateEntity
import com.farmledger.app.data.local.entity.InventoryItemEntity
import com.farmledger.app.data.local.entity.LedgerEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries WHERE localDate = :date ORDER BY createdAtEpochMs DESC")
    fun observeByDate(date: String): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE localDate = :date ORDER BY createdAtEpochMs DESC")
    suspend fun getByDate(date: String): List<LedgerEntryEntity>

    @Query("SELECT * FROM ledger_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LedgerEntryEntity?

    @Query("SELECT * FROM ledger_entries ORDER BY localDate DESC, createdAtEpochMs DESC")
    suspend fun getAll(): List<LedgerEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: LedgerEntryEntity)

    @Update
    suspend fun update(entry: LedgerEntryEntity)

    @Query("SELECT COUNT(*) FROM ledger_entries WHERE localDate = :date AND status = 'ACTIVE'")
    suspend fun activeCount(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<LedgerEntryEntity>)
}

@Dao
interface SettlementDao {
    @Query("SELECT * FROM daily_settlements WHERE localDate = :date LIMIT 1")
    suspend fun get(date: String): DailySettlementEntity?

    @Query("SELECT * FROM daily_settlements ORDER BY localDate DESC")
    suspend fun getAll(): List<DailySettlementEntity>

    @Query("SELECT * FROM daily_settlements WHERE localDate >= :from AND localDate <= :to")
    suspend fun getBetween(from: String, to: String): List<DailySettlementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: DailySettlementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<DailySettlementEntity>)

    @Query("SELECT COUNT(*) FROM daily_settlements")
    suspend fun countAll(): Int
}

@Dao
interface GameDayDao {
    @Query("SELECT * FROM game_day_state WHERE id = 1 LIMIT 1")
    fun observe(): Flow<GameDayStateEntity?>

    @Query("SELECT * FROM game_day_state WHERE id = 1 LIMIT 1")
    suspend fun get(): GameDayStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GameDayStateEntity)
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY id")
    fun observeAll(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items ORDER BY id")
    suspend fun getAll(): List<InventoryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<InventoryItemEntity>)

    @Query("DELETE FROM inventory_items")
    suspend fun clear()
}
