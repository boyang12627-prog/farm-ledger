package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.LedgerCategory

/**
 * 牧場建造商店（種子幣；≠港幣帳）。
 *
 * 渲染鐵則（day1_empty.json）：
 * `show = shop_day_reached AND owned`；否則完全隱藏（唔灰桩）。
 *
 * 日程／日結只控**上架**；場上可見唔因日子自動浮現。
 */
data class RanchBuildCatalogItem(
    val id: String,
    val category: LedgerCategory,
    val nameZh: String,
    val priceCoins: Int
)

data class ShopContext(
    val totalSettleDays: Int,
    val ownedIds: Set<String>,
    /** 首次對帳成功（ACTIVE TRANSFER）；唔用日結日數代替 */
    val hasReconcileSuccess: Boolean = false
)

data class RanchBuildPurchaseResult(
    val ok: Boolean,
    val msg: String,
    val seedCoinDelta: Int = 0,
    val ownedIds: Set<String> = emptySet()
)

object RanchBuildShopLogic {

    val catalog: List<RanchBuildCatalogItem> = listOf(
        RanchBuildCatalogItem("home", LedgerCategory.HOUSING, "門廊", 8),
        RanchBuildCatalogItem("food", LedgerCategory.FOOD, "灶／餐桌", 12),
        RanchBuildCatalogItem("income", LedgerCategory.INCOME, "郵箱", 12),
        RanchBuildCatalogItem("daily", LedgerCategory.DAILY, "木箱", 16),
        RanchBuildCatalogItem("transit", LedgerCategory.TRANSPORT, "單車", 20),
        RanchBuildCatalogItem("save", LedgerCategory.SAVINGS, "撲滿", 24),
        RanchBuildCatalogItem("fun", LedgerCategory.ENTERTAINMENT, "花圃", 28),
        RanchBuildCatalogItem("health", LedgerCategory.HEALTH, "藥草", 28),
        RanchBuildCatalogItem("other", LedgerCategory.OTHER, "告示牌", 16)
    )

    fun item(id: String): RanchBuildCatalogItem? = catalog.find { it.id == id }

    fun item(category: LedgerCategory): RanchBuildCatalogItem? =
        catalog.find { it.category == category }

    /** 商店是否開門（完成第1次日結後） */
    fun isShopOpen(totalSettleDays: Int): Boolean = totalSettleDays >= 1

    fun isOwned(id: String, ownedIds: Set<String>): Boolean = id in ownedIds

    /**
     * 上架日／條件已到（唔理擁有）。對齊策劃鎖死表＋day1_empty build_order。
     */
    fun shopDayReached(id: String, ctx: ShopContext): Boolean = when (id) {
        "home" -> ctx.totalSettleDays >= 1
        "food", "income" ->
            "home" in ctx.ownedIds || ctx.totalSettleDays >= 2
        "daily" ->
            ctx.totalSettleDays >= 3 || "food" in ctx.ownedIds
        "transit" -> ctx.totalSettleDays >= 5
        "save" ->
            ctx.totalSettleDays >= 7 || ctx.hasReconcileSuccess
        "fun" -> ctx.totalSettleDays >= 10
        "health" -> ctx.totalSettleDays >= 12
        "other" -> ctx.totalSettleDays >= 14
        else -> false
    }

    /**
     * 場上渲染閘：shop_day_reached AND owned；否則隱藏。
     */
    fun isRenderable(id: String, ctx: ShopContext): Boolean =
        id in ctx.ownedIds && shopDayReached(id, ctx)

    fun isRenderable(category: LedgerCategory, ctx: ShopContext): Boolean =
        isRenderable(HotspotUnlockLogic.unlockId(category), ctx)

    fun visibleCategories(ctx: ShopContext): Set<LedgerCategory> =
        catalog.filter { isRenderable(it.id, ctx) }.map { it.category }.toSet()

    /** 已擁有唔再上架；未開門＝無上架項 */
    fun isListed(id: String, ctx: ShopContext): Boolean {
        if (id in ctx.ownedIds) return false
        if (!isShopOpen(ctx.totalSettleDays)) return false
        return shopDayReached(id, ctx)
    }

    fun listedItems(ctx: ShopContext): List<RanchBuildCatalogItem> =
        catalog.filter { isListed(it.id, ctx) }

    fun canAfford(id: String, seedCoins: Int): Boolean {
        val price = item(id)?.priceCoins ?: return false
        return seedCoins >= price
    }

    fun purchase(
        id: String,
        seedCoins: Int,
        ctx: ShopContext
    ): RanchBuildPurchaseResult {
        val item = item(id)
            ?: return RanchBuildPurchaseResult(false, "無此建造項目。", ownedIds = ctx.ownedIds)
        if (!isShopOpen(ctx.totalSettleDays)) {
            return RanchBuildPurchaseResult(
                false,
                "日結後先可建造呀。",
                ownedIds = ctx.ownedIds
            )
        }
        if (id in ctx.ownedIds) {
            return RanchBuildPurchaseResult(
                false,
                "「${item.nameZh}」已經有啦。",
                ownedIds = ctx.ownedIds
            )
        }
        if (!isListed(id, ctx)) {
            return RanchBuildPurchaseResult(
                false,
                "「${item.nameZh}」尚未上架。",
                ownedIds = ctx.ownedIds
            )
        }
        if (seedCoins < item.priceCoins) {
            return RanchBuildPurchaseResult(
                false,
                "種子幣不足（有 $seedCoins，需要 ${item.priceCoins}）。牧場經濟，唔入真帳。",
                ownedIds = ctx.ownedIds
            )
        }
        return RanchBuildPurchaseResult(
            ok = true,
            msg = "已建造「${item.nameZh}」（−${item.priceCoins} 種子幣；牧場經濟，唔入港幣帳）",
            seedCoinDelta = -item.priceCoins,
            ownedIds = ctx.ownedIds + id
        )
    }
}
