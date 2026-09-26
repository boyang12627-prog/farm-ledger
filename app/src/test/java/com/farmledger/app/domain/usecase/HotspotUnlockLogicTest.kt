package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

class HotspotUnlockLogicTest {

    @Test
    fun day1_onlyThreeUnlocked_foodHomeIncome() {
        val unlocked = HotspotUnlockLogic.unlockedCategories(ranchDay = 1)
        assertThat(unlocked).containsExactly(
            LedgerCategory.FOOD,
            LedgerCategory.HOUSING,
            LedgerCategory.INCOME
        )
        assertThat(unlocked).hasSize(3)
        for (c in LedgerCategory.entries) {
            if (c in unlocked) {
                assertThat(HotspotUnlockLogic.mode(c, 1))
                    .isEqualTo(HotspotUnlockMode.UNLOCKED)
            } else {
                assertThat(HotspotUnlockLogic.mode(c, 1))
                    .isEqualTo(HotspotUnlockMode.HIDDEN)
            }
        }
    }

    @Test
    fun day2_stillOnlyThree() {
        assertThat(HotspotUnlockLogic.unlockedCategories(ranchDay = 2)).containsExactly(
            LedgerCategory.FOOD,
            LedgerCategory.HOUSING,
            LedgerCategory.INCOME
        )
    }

    @Test
    fun day3_addsDaily() {
        val unlocked = HotspotUnlockLogic.unlockedCategories(ranchDay = 3)
        assertThat(unlocked).contains(LedgerCategory.DAILY)
        assertThat(unlocked).containsAtLeast(
            LedgerCategory.FOOD,
            LedgerCategory.HOUSING,
            LedgerCategory.INCOME,
            LedgerCategory.DAILY
        )
        assertThat(unlocked).doesNotContain(LedgerCategory.TRANSPORT)
        assertThat(unlocked).doesNotContain(LedgerCategory.SAVINGS)
    }

    @Test
    fun day5_addsTransit() {
        assertThat(HotspotUnlockLogic.isVisible(LedgerCategory.TRANSPORT, 5)).isTrue()
        assertThat(HotspotUnlockLogic.isVisible(LedgerCategory.TRANSPORT, 4)).isFalse()
    }

    @Test
    fun day7_addsSave() {
        assertThat(HotspotUnlockLogic.isVisible(LedgerCategory.SAVINGS, 7)).isTrue()
        assertThat(HotspotUnlockLogic.isVisible(LedgerCategory.SAVINGS, 6)).isFalse()
    }

    @Test
    fun day10_12_14_funHealthOther() {
        assertThat(HotspotUnlockLogic.isVisible(LedgerCategory.ENTERTAINMENT, 10)).isTrue()
        assertThat(HotspotUnlockLogic.isVisible(LedgerCategory.HEALTH, 12)).isTrue()
        assertThat(HotspotUnlockLogic.isVisible(LedgerCategory.OTHER, 14)).isTrue()
        assertThat(HotspotUnlockLogic.isVisible(LedgerCategory.OTHER, 13)).isFalse()
    }

    @Test
    fun earlyDaily_whenExpenseCountAtLeast3() {
        assertThat(
            HotspotUnlockLogic.mode(
                LedgerCategory.DAILY,
                ranchDay = 1,
                expenseEntryCount = 3
            )
        ).isEqualTo(HotspotUnlockMode.UNLOCKED)
        assertThat(
            HotspotUnlockLogic.mode(
                LedgerCategory.DAILY,
                ranchDay = 1,
                expenseEntryCount = 2
            )
        ).isEqualTo(HotspotUnlockMode.HIDDEN)
    }

    @Test
    fun earlySave_whenTransferOrSettle() {
        assertThat(
            HotspotUnlockLogic.mode(
                LedgerCategory.SAVINGS,
                ranchDay = 1,
                hasSavingsOrReconcileSuccess = true
            )
        ).isEqualTo(HotspotUnlockMode.UNLOCKED)
        assertThat(
            HotspotUnlockLogic.mode(
                LedgerCategory.SAVINGS,
                ranchDay = 1,
                hasSavingsOrReconcileSuccess = false
            )
        ).isEqualTo(HotspotUnlockMode.HIDDEN)
    }

    @Test
    fun activeExpenseCount_countsOnlyActiveExpenses() {
        val entries = listOf(
            entry("1", EntryType.EXPENSE, EntryStatus.ACTIVE),
            entry("2", EntryType.EXPENSE, EntryStatus.ACTIVE),
            entry("3", EntryType.EXPENSE, EntryStatus.VOIDED),
            entry("4", EntryType.INCOME, EntryStatus.ACTIVE),
            entry("5", EntryType.TRANSFER, EntryStatus.ACTIVE)
        )
        assertThat(HotspotUnlockLogic.activeExpenseCount(entries)).isEqualTo(2)
    }

    @Test
    fun hasSavingsOrReconcile_transferOrSettleDays() {
        val none = listOf(entry("1", EntryType.EXPENSE, EntryStatus.ACTIVE))
        assertThat(HotspotUnlockLogic.hasSavingsOrReconcileSuccess(none, 0)).isFalse()
        assertThat(HotspotUnlockLogic.hasSavingsOrReconcileSuccess(none, 1)).isTrue()
        val xfer = listOf(entry("t", EntryType.TRANSFER, EntryStatus.ACTIVE))
        assertThat(HotspotUnlockLogic.hasSavingsOrReconcileSuccess(xfer, 0)).isTrue()
    }

    @Test
    fun unlockIds_matchDay1SparseJson() {
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.HOUSING)).isEqualTo("home")
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.FOOD)).isEqualTo("food")
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.INCOME)).isEqualTo("income")
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.DAILY)).isEqualTo("daily")
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.TRANSPORT)).isEqualTo("transit")
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.SAVINGS)).isEqualTo("save")
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.ENTERTAINMENT)).isEqualTo("fun")
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.HEALTH)).isEqualTo("health")
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.OTHER)).isEqualTo("other")
        assertThat(HotspotUnlockLogic.categoryOfUnlockId("transit"))
            .isEqualTo(LedgerCategory.TRANSPORT)
    }

    @Test
    fun bottomBarEntry_notGatedByUnlock_allCategoriesStillExist() {
        // 鐵則：未解鎖只隱藏地圖捷徑；分類 enum／expenseChips 仍完整可選
        assertThat(LedgerCategory.entries).hasSize(9)
        assertThat(LedgerCategory.expenseChips).containsAtLeast(
            LedgerCategory.DAILY,
            LedgerCategory.TRANSPORT,
            LedgerCategory.ENTERTAINMENT,
            LedgerCategory.HEALTH,
            LedgerCategory.OTHER
        )
        // unlock HIDDEN 唔刪除分類
        assertThat(HotspotUnlockLogic.isVisible(LedgerCategory.OTHER, 1)).isFalse()
        assertThat(LedgerCategory.fromStorage("OTHER")).isEqualTo(LedgerCategory.OTHER)
    }

    private fun entry(id: String, type: EntryType, status: EntryStatus) = LedgerEntry(
        id = id,
        localDate = "2026-09-26",
        type = type,
        amountMinor = 100,
        note = "",
        category = LedgerCategory.FOOD.name,
        status = status,
        createdAtEpochMs = 1,
        updatedAtEpochMs = 1
    )
}

@RunWith(Parameterized::class)
class HotspotUnlockScheduleTableTest(
    private val day: Int,
    private val expectedIds: Set<String>
) {
    @Test
    fun scheduleMatchesDay1SparseJson() {
        val unlocked = HotspotUnlockLogic.unlockedCategories(ranchDay = day)
        val ids = unlocked.map { HotspotUnlockLogic.unlockId(it) }.toSet()
        assertThat(ids).isEqualTo(expectedIds)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "day={0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(1, setOf("home", "food", "income")),
            arrayOf(2, setOf("home", "food", "income")),
            arrayOf(3, setOf("home", "food", "income", "daily")),
            arrayOf(5, setOf("home", "food", "income", "daily", "transit")),
            arrayOf(7, setOf("home", "food", "income", "daily", "transit", "save")),
            arrayOf(10, setOf("home", "food", "income", "daily", "transit", "save", "fun")),
            arrayOf(12, setOf("home", "food", "income", "daily", "transit", "save", "fun", "health")),
            arrayOf(
                14,
                setOf(
                    "home", "food", "income", "daily", "transit", "save", "fun", "health", "other"
                )
            )
        )
    }
}
