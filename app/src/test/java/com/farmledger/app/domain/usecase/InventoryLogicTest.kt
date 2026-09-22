package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.InventoryItemKind
import com.farmledger.app.domain.model.ShopCatalog
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InventoryLogicTest {

    private val empty = InventoryLogic.emptyStubs(0L)

    @Test
    fun buySeeds_writesExpenseDraft_addsInventory_noGrowthInTrade() {
        val r = InventoryLogic.buySeeds(empty, CropKind.WHEAT, 2, nowEpochMs = 10L)
        assertThat(r.ok).isTrue()
        assertThat(r.entryType).isEqualTo(EntryType.EXPENSE)
        assertThat(r.amountMinor).isEqualTo(ShopCatalog.seedBuyPriceMinor(CropKind.WHEAT) * 2)
        assertThat(InventoryLogic.seedQty(r.inventory, CropKind.WHEAT)).isEqualTo(2)
        assertThat(r.note).contains("買")
        assertThat(r.msg).contains("唔發成長點")
    }

    @Test
    fun sellHarvest_requiresInventory_writesIncome() {
        val withCrop = InventoryLogic.add(empty, InventoryItemKind.CROP_CARROT, 3, 1L).inventory
        val fail = InventoryLogic.sellHarvest(empty, CropKind.CARROT, 1, 2L)
        assertThat(fail.ok).isFalse()

        val ok = InventoryLogic.sellHarvest(withCrop, CropKind.CARROT, 2, 3L)
        assertThat(ok.ok).isTrue()
        assertThat(ok.entryType).isEqualTo(EntryType.INCOME)
        assertThat(ok.amountMinor).isEqualTo(ShopCatalog.cropSellPriceMinor(CropKind.CARROT) * 2)
        assertThat(InventoryLogic.cropQty(ok.inventory, CropKind.CARROT)).isEqualTo(1)
        assertThat(ok.msg).contains("唔發成長點")
    }

    @Test
    fun tradeAmounts_doNotImplyGrowthPoints() {
        // 買賣只回傳帳本草稿；成長點發放屬 DailySettlementLogic，與此正交
        val buy = InventoryLogic.buySeeds(empty, CropKind.TOMATO, 5, 1L)
        val sellInv = InventoryLogic.add(empty, InventoryItemKind.CROP_TOMATO, 5, 1L).inventory
        val sell = InventoryLogic.sellHarvest(sellInv, CropKind.TOMATO, 5, 2L)
        assertThat(buy.amountMinor).isGreaterThan(0L)
        assertThat(sell.amountMinor).isGreaterThan(0L)
        // TradeResult 無 growthPoints 欄位；金額再大也不走結算
        assertThat(buy.entryType).isEqualTo(EntryType.EXPENSE)
        assertThat(sell.entryType).isEqualTo(EntryType.INCOME)
    }

    @Test
    fun remove_failsWhenInsufficient() {
        val r = InventoryLogic.remove(empty, InventoryItemKind.SEED_WHEAT, 1, 1L)
        assertThat(r.ok).isFalse()
        assertThat(r.msg).contains("不足")
    }

    @Test
    fun grantStreakSeeds_addsWheatSeeds() {
        val inv = InventoryLogic.grantStreakSeeds(empty, 2, 5L)
        assertThat(InventoryLogic.seedQty(inv, CropKind.WHEAT)).isEqualTo(2)
    }

    @Test
    fun legacySeedBag_mapsToWheat() {
        assertThat(InventoryItemKind.fromStorage("SEED_BAG")).isEqualTo(InventoryItemKind.SEED_WHEAT)
    }

    @Test
    fun buyFeed_writesExpense_addsFeed_noGrowthInTrade() {
        val r = InventoryLogic.buyFeed(empty, 3, nowEpochMs = 10L)
        assertThat(r.ok).isTrue()
        assertThat(r.entryType).isEqualTo(EntryType.EXPENSE)
        assertThat(r.amountMinor).isEqualTo(ShopCatalog.FEED_BUY_PRICE_MINOR * 3)
        assertThat(InventoryLogic.feedQty(r.inventory)).isEqualTo(3)
        assertThat(r.msg).contains("唔發成長點")
    }
}
