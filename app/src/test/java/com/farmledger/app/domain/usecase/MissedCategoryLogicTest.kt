package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MissedCategoryLogicTest {
    private fun entry(cat: LedgerCategory?, date: String = "2026-09-26") = LedgerEntry(
        id = "e-${cat?.name}",
        localDate = date,
        type = EntryType.EXPENSE,
        amountMinor = 100,
        category = cat?.name,
        status = EntryStatus.ACTIVE,
        createdAtEpochMs = 1,
        updatedAtEpochMs = 1
    )

    @Test
    fun missed_allNine_whenNoEntries() {
        val missed = MissedCategoryLogic.missedCategories(emptyList(), "2026-09-26")
        assertThat(missed).containsExactlyElementsIn(LedgerCategory.entries.toSet())
    }

    @Test
    fun missed_hidesGrass_whenCategoryRecorded() {
        val entries = listOf(entry(LedgerCategory.FOOD), entry(LedgerCategory.TRANSPORT))
        val missed = MissedCategoryLogic.missedCategories(entries, "2026-09-26")
        assertThat(missed).doesNotContain(LedgerCategory.FOOD)
        assertThat(missed).doesNotContain(LedgerCategory.TRANSPORT)
        assertThat(missed).contains(LedgerCategory.HEALTH)
    }

    @Test
    fun missedExpenseChips_whenNothingRecorded_returnsAllExpenseChips() {
        val missed = MissedCategoryLogic.missedExpenseChips(emptyList(), "2026-09-26")
        assertThat(missed).isEqualTo(LedgerCategory.expenseChips)
    }

    @Test
    fun weedStackCount_capsAtThree() {
        assertThat(MissedCategoryLogic.weedStackCount(0)).isEqualTo(0)
        assertThat(MissedCategoryLogic.weedStackCount(1)).isEqualTo(1)
        assertThat(MissedCategoryLogic.weedStackCount(5)).isEqualTo(3)
    }
}
