package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HotspotUnlockLogicTest {

    @Test
    fun day1_ownedEmpty_visibleHotspotsEmpty() {
        val ctx = ShopContext(totalSettleDays = 0, ownedIds = emptySet())
        assertThat(HotspotUnlockLogic.unlockedCategories(ctx)).isEmpty()
        for (c in LedgerCategory.entries) {
            assertThat(HotspotUnlockLogic.mode(c, ctx)).isEqualTo(HotspotUnlockMode.HIDDEN)
        }
    }

    @Test
    fun ownedWithoutShopDay_stillHidden() {
        // shop_day AND owned：未日結即使標記 owned 都唔畫
        val ctx = ShopContext(totalSettleDays = 0, ownedIds = setOf("home"))
        assertThat(HotspotUnlockLogic.isVisible(LedgerCategory.HOUSING, ctx)).isFalse()
    }

    @Test
    fun ownedHome_afterSettle1_visibleOnlyHome() {
        val ctx = ShopContext(totalSettleDays = 1, ownedIds = setOf("home"))
        assertThat(HotspotUnlockLogic.unlockedCategories(ctx))
            .containsExactly(LedgerCategory.HOUSING)
    }

    @Test
    fun unlockIds_matchCatalog() {
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.HOUSING)).isEqualTo("home")
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.FOOD)).isEqualTo("food")
        assertThat(HotspotUnlockLogic.unlockId(LedgerCategory.INCOME)).isEqualTo("income")
        assertThat(HotspotUnlockLogic.categoryOfUnlockId("transit"))
            .isEqualTo(LedgerCategory.TRANSPORT)
    }

    @Test
    fun bottomBarEntry_notGatedByOwned_allCategoriesStillExist() {
        assertThat(LedgerCategory.entries).hasSize(9)
        assertThat(LedgerCategory.expenseChips).containsAtLeast(
            LedgerCategory.DAILY,
            LedgerCategory.TRANSPORT,
            LedgerCategory.ENTERTAINMENT,
            LedgerCategory.HEALTH,
            LedgerCategory.OTHER
        )
        assertThat(HotspotUnlockLogic.isVisible(LedgerCategory.OTHER, emptySet())).isFalse()
        assertThat(LedgerCategory.fromStorage("OTHER")).isEqualTo(LedgerCategory.OTHER)
    }

    @Test
    fun hasReconcileSuccess_transferOnly() {
        val none = listOf(entry("1", EntryType.EXPENSE, EntryStatus.ACTIVE))
        assertThat(HotspotUnlockLogic.hasReconcileSuccess(none)).isFalse()
        val xfer = listOf(entry("t", EntryType.TRANSFER, EntryStatus.ACTIVE))
        assertThat(HotspotUnlockLogic.hasReconcileSuccess(xfer)).isTrue()
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

class RanchBuildShopLogicTest {

    @Test
    fun shopClosed_beforeFirstSettle() {
        assertThat(RanchBuildShopLogic.isShopOpen(0)).isFalse()
        val ctx = ShopContext(0, emptySet())
        assertThat(RanchBuildShopLogic.listedItems(ctx)).isEmpty()
    }

    @Test
    fun settle1_listsHomeAt8() {
        val ctx = ShopContext(totalSettleDays = 1, ownedIds = emptySet())
        val listed = RanchBuildShopLogic.listedItems(ctx)
        assertThat(listed.map { it.id }).containsExactly("home")
        assertThat(RanchBuildShopLogic.item("home")!!.priceCoins).isEqualTo(8)
    }

    @Test
    fun purchaseHome_deductsCoins_andOwns() {
        val ctx = ShopContext(totalSettleDays = 1, ownedIds = emptySet())
        val r = RanchBuildShopLogic.purchase("home", seedCoins = 20, ctx = ctx)
        assertThat(r.ok).isTrue()
        assertThat(r.seedCoinDelta).isEqualTo(-8)
        assertThat(r.ownedIds).containsExactly("home")
        val after = ShopContext(1, r.ownedIds)
        assertThat(RanchBuildShopLogic.visibleCategories(after))
            .containsExactly(LedgerCategory.HOUSING)
        // food/income 因已建 home 而上架
        assertThat(RanchBuildShopLogic.listedItems(after).map { it.id }.toSet())
            .containsAtLeast("food", "income")
    }

    @Test
    fun cannotAfford_rejected() {
        val ctx = ShopContext(1, emptySet())
        val r = RanchBuildShopLogic.purchase("home", seedCoins = 3, ctx = ctx)
        assertThat(r.ok).isFalse()
        assertThat(r.ownedIds).isEmpty()
    }

    @Test
    fun unowned_neverRenderable() {
        val ctx = ShopContext(totalSettleDays = 14, ownedIds = emptySet())
        assertThat(RanchBuildShopLogic.visibleCategories(ctx)).isEmpty()
    }
}
