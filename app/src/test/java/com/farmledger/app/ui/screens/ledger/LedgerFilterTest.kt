package com.farmledger.app.ui.screens.ledger

import com.farmledger.app.domain.model.DefaultAccounts
import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerEntry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LedgerFilterTest {
    private var seq = 0
    private fun e(
        note: String = "",
        type: EntryType = EntryType.EXPENSE,
        accountId: String = DefaultAccounts.CASH_ID,
        transferAccountId: String? = null,
        amountMinor: Long = 4200
    ) = LedgerEntry(
        id = "e-${++seq}",
        localDate = "2026-09-26",
        type = type,
        amountMinor = amountMinor,
        note = note,
        accountId = accountId,
        transferAccountId = transferAccountId,
        status = EntryStatus.ACTIVE,
        createdAtEpochMs = 1,
        updatedAtEpochMs = 1
    )

    @Test
    fun filter_matchesAccountName() {
        val names = mapOf("cash" to "現金", "bank" to "銀行")
        val list = listOf(e(accountId = "bank"), e(accountId = "cash", note = "咖啡"))
        assertThat(filterEntries(list, "銀行", names)).hasSize(1)
        assertThat(filterEntries(list, "現金", names)).hasSize(1)
        assertThat(filterEntries(list, "咖啡", names)).hasSize(1)
    }

    @Test
    fun filter_matchesTransferAccounts() {
        val names = mapOf("cash" to "現金", "payme" to "PayMe")
        val list = listOf(
            e(type = EntryType.TRANSFER, accountId = "cash", transferAccountId = "payme", amountMinor = 10000)
        )
        assertThat(filterEntries(list, "PayMe", names)).hasSize(1)
        assertThat(filterEntries(list, "轉帳", names)).hasSize(1)
    }

    @Test
    fun accountLine_transferShowsArrow() {
        val names = mapOf("cash" to "現金", "card" to "信用卡")
        val line = accountLine(
            e(type = EntryType.TRANSFER, accountId = "cash", transferAccountId = "card"),
            names
        )
        assertThat(line).contains("現金")
        assertThat(line).contains("信用卡")
        assertThat(line).contains("→")
    }
}
