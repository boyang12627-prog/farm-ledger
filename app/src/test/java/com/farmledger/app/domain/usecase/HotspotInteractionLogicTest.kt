package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HotspotInteractionLogicTest {

    @Test
    fun savings_disabled_whenFewerThanTwoAccounts() {
        assertThat(HotspotInteractionLogic.isEnabled(LedgerCategory.SAVINGS, 1)).isFalse()
        assertThat(HotspotInteractionLogic.isEnabled(LedgerCategory.SAVINGS, 0)).isFalse()
        assertThat(HotspotInteractionLogic.isEnabled(LedgerCategory.SAVINGS, 2)).isTrue()
        assertThat(HotspotInteractionLogic.isEnabled(LedgerCategory.FOOD, 1)).isTrue()
    }

    @Test
    fun visualState_pressed_vs_disabled() {
        assertThat(
            HotspotInteractionLogic.visualState(LedgerCategory.FOOD, 2, pressed = true)
        ).isEqualTo(HotspotVisualState.PRESSED)
        assertThat(
            HotspotInteractionLogic.visualState(LedgerCategory.SAVINGS, 1, pressed = true)
        ).isEqualTo(HotspotVisualState.DISABLED)
        assertThat(
            HotspotInteractionLogic.visualState(LedgerCategory.FOOD, 2, pressed = false)
        ).isEqualTo(HotspotVisualState.IDLE)
    }

    @Test
    fun floatingLabel_piggyShowsTransfer() {
        assertThat(HotspotInteractionLogic.floatingLabel(LedgerCategory.SAVINGS)).isEqualTo("轉帳")
        assertThat(HotspotInteractionLogic.floatingLabel(LedgerCategory.FOOD)).isEqualTo("飲食")
    }

    @Test
    fun loggedTodayBadge_independentOfEnabled() {
        val set = setOf(LedgerCategory.FOOD)
        assertThat(HotspotInteractionLogic.loggedTodayBadge(set, LedgerCategory.FOOD)).isTrue()
        assertThat(HotspotInteractionLogic.loggedTodayBadge(set, LedgerCategory.HEALTH)).isFalse()
    }

    @Test
    fun latestEntrySummary_picksNewestActive() {
        val entries = listOf(
            LedgerEntry(
                id = "a", localDate = "2026-09-25", type = EntryType.EXPENSE,
                amountMinor = 1000, note = "舊", category = LedgerCategory.FOOD.name,
                status = EntryStatus.ACTIVE, createdAtEpochMs = 1, updatedAtEpochMs = 1
            ),
            LedgerEntry(
                id = "b", localDate = "2026-09-26", type = EntryType.EXPENSE,
                amountMinor = 2550, note = "茶餐廳", category = LedgerCategory.FOOD.name,
                status = EntryStatus.ACTIVE, createdAtEpochMs = 2, updatedAtEpochMs = 99
            )
        )
        val s = HotspotInteractionLogic.latestEntrySummary(entries, LedgerCategory.FOOD)
        assertThat(s).contains("HK$25.50")
        assertThat(s).contains("茶餐廳")
    }

    @Test
    fun whisperShortName_isAtMostFourChars() {
        for (c in LedgerCategory.entries) {
            val s = HotspotInteractionLogic.whisperShortName(c)
            assertThat(s.length).isAtMost(4)
            assertThat(s).isEqualTo(c.nameZh.take(4))
        }
    }
}
