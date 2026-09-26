package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.InventoryItemKind
import com.farmledger.app.domain.model.ShopCatalog
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InventoryLogicTest {

    private val empty = InventoryLogic.emptyStubs(0L)

    @Test
    fun buySeeds_deductsSeedCoins_addsInventory_noLedgerDraft() {
        val coins = 20
        val r = InventoryLogic.buySeeds(empty, CropKind.WHEAT, 2, nowEpochMs = 10L, seedCoins = coins)
        assertThat(r.ok).isTrue()
        assertThat(r.seedCoinDelta).isEqualTo(-(ShopCatalog.seedBuyPriceCoins(CropKind.WHEAT) * 2))
        assertThat(InventoryLogic.seedQty(r.inventory, CropKind.WHEAT)).isEqualTo(2)
        assertThat(r.msg).contains("唔入港幣帳")
        assertThat(r.msg).contains("唔發成長點")
    }

    @Test
    fun buySeeds_failsWhenInsufficientCoins() {
        val r = InventoryLogic.buySeeds(empty, CropKind.TOMATO, 5, nowEpochMs = 10L, seedCoins = 1)
        assertThat(r.ok).isFalse()
        assertThat(r.msg).contains("種子幣不足")
        assertThat(r.seedCoinDelta).isEqualTo(0)
    }

    @Test
    fun sellHarvest_requiresInventory_grantsSeedCoins_noLedger() {
        val withCrop = InventoryLogic.add(empty, InventoryItemKind.CROP_CARROT, 3, 1L).inventory
        val fail = InventoryLogic.sellHarvest(empty, CropKind.CARROT, 1, 2L)
        assertThat(fail.ok).isFalse()

        val ok = InventoryLogic.sellHarvest(withCrop, CropKind.CARROT, 2, 3L)
        assertThat(ok.ok).isTrue()
        assertThat(ok.seedCoinDelta).isEqualTo(ShopCatalog.cropSellPriceCoins(CropKind.CARROT) * 2)
        assertThat(InventoryLogic.cropQty(ok.inventory, CropKind.CARROT)).isEqualTo(1)
        assertThat(ok.msg).contains("唔入港幣帳")
    }

    @Test
    fun tradeAmounts_areSeedCoins_notHkdLedger() {
        val buy = InventoryLogic.buySeeds(empty, CropKind.TOMATO, 1, 1L, seedCoins = 100)
        val sellInv = InventoryLogic.add(empty, InventoryItemKind.CROP_TOMATO, 5, 1L).inventory
        val sell = InventoryLogic.sellHarvest(sellInv, CropKind.TOMATO, 5, 2L)
        assertThat(buy.seedCoinDelta).isLessThan(0)
        assertThat(sell.seedCoinDelta).isGreaterThan(0)
        // TradeResult 無 entryType／amountMinor；唔會寫 ledger_entries
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
    fun buyFeed_deductsSeedCoins_addsFeed_noLedger() {
        val r = InventoryLogic.buyFeed(empty, 3, nowEpochMs = 10L, seedCoins = 20)
        assertThat(r.ok).isTrue()
        assertThat(r.seedCoinDelta).isEqualTo(-(ShopCatalog.FEED_BUY_PRICE_COINS * 3))
        assertThat(InventoryLogic.feedQty(r.inventory)).isEqualTo(3)
        assertThat(r.msg).contains("唔入港幣帳")
    }
}
