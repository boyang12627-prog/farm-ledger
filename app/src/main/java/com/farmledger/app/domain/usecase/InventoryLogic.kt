package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.CropTimers
import com.farmledger.app.domain.model.InventoryItem
import com.farmledger.app.domain.model.InventoryItemKind
import com.farmledger.app.domain.model.ShopCatalog

data class InventoryMutation(
    val inventory: List<InventoryItem>,
    val ok: Boolean,
    val msg: String
)

/**
 * 牧場買賣結果：只改背包＋種子幣增量。
 * **唔寫真港幣帳**、**不發成長點**——成長點只由每日結算發放。
 */
data class TradeResult(
    val inventory: List<InventoryItem>,
    /** 種子幣變動：買為負、賣為正；唔入 ledger_entries */
    val seedCoinDelta: Int,
    val ok: Boolean,
    val msg: String
)

object InventoryLogic {

    fun emptyStubs(nowEpochMs: Long = 0L): List<InventoryItem> =
        InventoryItemKind.entries.map { kind ->
            InventoryItem(
                id = kind.storageId(),
                kind = kind,
                quantity = 0,
                updatedAtEpochMs = nowEpochMs
            )
        }

    fun qty(inventory: List<InventoryItem>, kind: InventoryItemKind): Int =
        inventory.find { it.kind == kind }?.quantity ?: 0

    fun seedQty(inventory: List<InventoryItem>, crop: CropKind): Int =
        qty(inventory, InventoryItemKind.seedOf(crop))

    fun cropQty(inventory: List<InventoryItem>, crop: CropKind): Int =
        qty(inventory, InventoryItemKind.cropOf(crop))

    fun feedQty(inventory: List<InventoryItem>): Int =
        qty(inventory, InventoryItemKind.FEED)

    fun totalSeeds(inventory: List<InventoryItem>): Int =
        CropKind.entries.sumOf { seedQty(inventory, it) }

    fun totalCrops(inventory: List<InventoryItem>): Int =
        CropKind.entries.sumOf { cropQty(inventory, it) }

    fun ensureKinds(inventory: List<InventoryItem>, nowEpochMs: Long): List<InventoryItem> {
        val byKind = inventory.associateBy { it.kind }.toMutableMap()
        InventoryItemKind.entries.forEach { kind ->
            if (kind !in byKind) {
                byKind[kind] = InventoryItem(
                    id = kind.storageId(),
                    kind = kind,
                    quantity = 0,
                    updatedAtEpochMs = nowEpochMs
                )
            }
        }
        return InventoryItemKind.entries.map { byKind.getValue(it) }
    }

    fun add(
        inventory: List<InventoryItem>,
        kind: InventoryItemKind,
        delta: Int,
        nowEpochMs: Long
    ): InventoryMutation {
        if (delta <= 0) return InventoryMutation(inventory, false, "數量須為正。")
        val ensured = ensureKinds(inventory, nowEpochMs)
        val updated = ensured.map {
            if (it.kind == kind) {
                it.copy(quantity = it.quantity + delta, updatedAtEpochMs = nowEpochMs)
            } else it
        }
        return InventoryMutation(updated, true, "已入庫 ${kind.nameZh} ×$delta")
    }

    fun remove(
        inventory: List<InventoryItem>,
        kind: InventoryItemKind,
        delta: Int,
        nowEpochMs: Long
    ): InventoryMutation {
        if (delta <= 0) return InventoryMutation(inventory, false, "數量須為正。")
        val ensured = ensureKinds(inventory, nowEpochMs)
        val have = qty(ensured, kind)
        if (have < delta) {
            return InventoryMutation(ensured, false, "${kind.nameZh}不足（有 $have，需要 $delta）。")
        }
        val updated = ensured.map {
            if (it.kind == kind) {
                it.copy(quantity = it.quantity - delta, updatedAtEpochMs = nowEpochMs)
            } else it
        }
        return InventoryMutation(updated, true, "已出庫 ${kind.nameZh} ×$delta")
    }

    /**
     * 買種子：入庫；扣種子幣（唔寫港幣帳、唔發成長點）。
     * @param seedCoins 當前種子幣（由 repository 傳入檢查）
     */
    fun buySeeds(
        inventory: List<InventoryItem>,
        crop: CropKind,
        quantity: Int,
        nowEpochMs: Long,
        seedCoins: Int
    ): TradeResult {
        if (quantity <= 0) {
            return TradeResult(inventory, 0, false, "購買數量須為正。")
        }
        val unit = ShopCatalog.seedBuyPriceCoins(crop)
        val total = unit * quantity
        if (seedCoins < total) {
            return TradeResult(
                inventory, 0, false,
                "種子幣不足（有 $seedCoins，需要 $total）。牧場內經濟，唔入真帳。"
            )
        }
        val kind = InventoryItemKind.seedOf(crop)
        val added = add(inventory, kind, quantity, nowEpochMs)
        if (!added.ok) {
            return TradeResult(inventory, 0, false, added.msg)
        }
        val note = "買${FarmLogic.cropZh(crop)}種子 ×$quantity"
        return TradeResult(
            inventory = added.inventory,
            seedCoinDelta = -total,
            ok = true,
            msg = "已買入$note（−$total 種子幣；牧場經濟，唔入港幣帳、唔發成長點）"
        )
    }

    /**
     * 賣收成：出庫；加種子幣（唔寫港幣帳、唔發成長點）。
     */
    fun sellHarvest(
        inventory: List<InventoryItem>,
        crop: CropKind,
        quantity: Int,
        nowEpochMs: Long
    ): TradeResult {
        if (quantity <= 0) {
            return TradeResult(inventory, 0, false, "出售數量須為正。")
        }
        val kind = InventoryItemKind.cropOf(crop)
        val removed = remove(inventory, kind, quantity, nowEpochMs)
        if (!removed.ok) {
            return TradeResult(inventory, 0, false, removed.msg)
        }
        val unit = ShopCatalog.cropSellPriceCoins(crop)
        val total = unit * quantity
        val note = "賣${FarmLogic.cropZh(crop)} ×$quantity"
        return TradeResult(
            inventory = removed.inventory,
            seedCoinDelta = total,
            ok = true,
            msg = "已售出$note（＋$total 種子幣；牧場經濟，唔入港幣帳、唔發成長點）"
        )
    }

    /**
     * 買飼料：入庫 FEED；扣種子幣（唔寫港幣帳）。
     */
    fun buyFeed(
        inventory: List<InventoryItem>,
        quantity: Int,
        nowEpochMs: Long,
        seedCoins: Int
    ): TradeResult {
        if (quantity <= 0) {
            return TradeResult(inventory, 0, false, "購買數量須為正。")
        }
        val unit = ShopCatalog.FEED_BUY_PRICE_COINS
        val total = unit * quantity
        if (seedCoins < total) {
            return TradeResult(
                inventory, 0, false,
                "種子幣不足（有 $seedCoins，需要 $total）。牧場內經濟，唔入真帳。"
            )
        }
        val added = add(inventory, InventoryItemKind.FEED, quantity, nowEpochMs)
        if (!added.ok) {
            return TradeResult(inventory, 0, false, added.msg)
        }
        val note = "買飼料 ×$quantity"
        return TradeResult(
            inventory = added.inventory,
            seedCoinDelta = -total,
            ok = true,
            msg = "已買入$note（−$total 種子幣；牧場經濟，唔入港幣帳、唔發成長點）"
        )
    }

    /** 連續結算種子加成：入庫小麥種子（不發成長點） */
    fun grantStreakSeeds(
        inventory: List<InventoryItem>,
        seedCount: Int,
        nowEpochMs: Long
    ): List<InventoryItem> {
        if (seedCount <= 0) return ensureKinds(inventory, nowEpochMs)
        return add(inventory, InventoryItemKind.SEED_WHEAT, seedCount, nowEpochMs).inventory
    }

    fun harvestYield(crop: CropKind): Int = CropTimers.harvestYield(crop)
}

private fun InventoryItemKind.storageId(): String = name.lowercase()
