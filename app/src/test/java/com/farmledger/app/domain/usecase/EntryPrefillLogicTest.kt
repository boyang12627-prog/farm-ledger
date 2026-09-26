package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class EntryPrefillLogicTest {
    @Test
    fun savingsHotspotPrefillsTransferNotIncome() {
        assertEquals(EntryType.TRANSFER, EntryPrefillLogic.entryTypeForCategory(LedgerCategory.SAVINGS))
    }

    @Test
    fun incomeHotspotPrefillsIncome() {
        assertEquals(EntryType.INCOME, EntryPrefillLogic.entryTypeForCategory(LedgerCategory.INCOME))
    }

    @Test
    fun expenseHotspotsPrefillExpense() {
        assertEquals(EntryType.EXPENSE, EntryPrefillLogic.entryTypeForCategory(LedgerCategory.FOOD))
        assertEquals(EntryType.EXPENSE, EntryPrefillLogic.entryTypeForCategory(LedgerCategory.OTHER))
    }
}
