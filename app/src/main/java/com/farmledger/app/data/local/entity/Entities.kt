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
