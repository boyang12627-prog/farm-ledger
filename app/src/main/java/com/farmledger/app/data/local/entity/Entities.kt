package com.farmledger.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledger_entries")
data class LedgerEntryEntity(
    @PrimaryKey val id: String,
    val localDate: String,
    val type: String,
    val amountMinor: Long,
    val note: String,
    val status: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "daily_settlements")
data class DailySettlementEntity(
    @PrimaryKey val localDate: String,
    val growthPointsAwarded: Int,
    val settledAtEpochMs: Long
)

/** 單一列：遊戲日／時段／階段提示（離線存檔） */
@Entity(tableName = "game_day_state")
data class GameDayStateEntity(
    @PrimaryKey val id: Int = 1,
    val gameDay: Int,
    val phase: String,
    val farmStageHint: String,
    val lastWallClockEpochMs: Long,
    val updatedAtEpochMs: Long
)

/** M2 stub：背包列（尚無商店／入庫流程） */
@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val quantity: Int,
    val updatedAtEpochMs: Long
)
