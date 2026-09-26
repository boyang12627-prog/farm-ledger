package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeedStackLogicTest {

    private fun entry(cat: LedgerCategory, date: String, id: String = "$cat-$date") = LedgerEntry(
        id = id,
        localDate = date,
        type = EntryType.EXPENSE,
        amountMinor = 100,
        category = cat.name,
        status = EntryStatus.ACTIVE,
        createdAtEpochMs = 1,
        updatedAtEpochMs = 1
    )

    @Test
    fun weedStack_zeroWhenLoggedToday() {
        val entries = listOf(entry(LedgerCategory.FOOD, "2026-09-26"))
        assertThat(MissedCategoryLogic.consecutiveMissDays(entries, LedgerCategory.FOOD, "2026-09-26"))
            .isEqualTo(0)
        assertThat(MissedCategoryLogic.weedStackCount(0)).isEqualTo(0)
    }

    @Test
    fun weedStack_countsConsecutiveMisses_capsAt3() {
        // no food entries at all → lookback days of miss
        val miss = MissedCategoryLogic.consecutiveMissDays(emptyList(), LedgerCategory.FOOD, "2026-09-26")
        assertThat(miss).isAtLeast(3)
        assertThat(MissedCategoryLogic.weedStackCount(miss)).isEqualTo(3)
    }

    @Test
    fun weedStack_breaksOnPriorDayLog() {
        val entries = listOf(entry(LedgerCategory.FOOD, "2026-09-24"))
        // 25 + 26 missed = 2
        val miss = MissedCategoryLogic.consecutiveMissDays(entries, LedgerCategory.FOOD, "2026-09-26")
        assertThat(miss).isEqualTo(2)
        assertThat(MissedCategoryLogic.weedStackCount(miss)).isEqualTo(2)
    }

    @Test
    fun weedStacksByCategory_mapsAllNine() {
        val map = MissedCategoryLogic.weedStacksByCategory(emptyList(), "2026-09-26")
        assertThat(map).hasSize(LedgerCategory.entries.size)
        assertThat(map.values.all { it in 1..3 }).isTrue()
    }
}
