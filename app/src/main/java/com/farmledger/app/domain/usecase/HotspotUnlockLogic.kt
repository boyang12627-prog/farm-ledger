package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry

/**
 * 熱區地圖捷徑可見性（≠入帳分類可用性）。
 *
 * **鐵則（0.5.4e-bare／day1_empty.json）**：
 * `show = shop_day_reached AND owned`；否則 [HotspotUnlockMode.HIDDEN]
 * （唔畫、唔灰桩、無大木牌牆）。分類仍可經底欄「入帳」完整選。
 *
 * 開局 `ownedHotspotIds=[]` → 淨農地。日程只控商店上架，見 [RanchBuildShopLogic]。
 */
enum class HotspotUnlockMode {
    /** 唔畫物件／雜草／hit target */
    HIDDEN,
    /** 可見可點（扑满仍受帳戶閘 [HotspotInteractionLogic.isEnabled]） */
    UNLOCKED
}

object HotspotUnlockLogic {

    fun categoryOfUnlockId(id: String): LedgerCategory? = when (id) {
        "home" -> LedgerCategory.HOUSING
        "food" -> LedgerCategory.FOOD
        "income" -> LedgerCategory.INCOME
        "daily" -> LedgerCategory.DAILY
        "transit" -> LedgerCategory.TRANSPORT
        "save" -> LedgerCategory.SAVINGS
        "fun" -> LedgerCategory.ENTERTAINMENT
        "health" -> LedgerCategory.HEALTH
        "other" -> LedgerCategory.OTHER
        else -> null
    }

    fun unlockId(category: LedgerCategory): String = when (category) {
        LedgerCategory.HOUSING -> "home"
        LedgerCategory.FOOD -> "food"
        LedgerCategory.INCOME -> "income"
        LedgerCategory.DAILY -> "daily"
        LedgerCategory.TRANSPORT -> "transit"
        LedgerCategory.SAVINGS -> "save"
        LedgerCategory.ENTERTAINMENT -> "fun"
        LedgerCategory.HEALTH -> "health"
        LedgerCategory.OTHER -> "other"
    }

    fun activeExpenseCount(entries: List<LedgerEntry>): Int =
        entries.count { it.status == EntryStatus.ACTIVE && it.type == EntryType.EXPENSE }

    /** 首次對帳成功：ACTIVE TRANSFER（扑满商店提前上架） */
    fun hasReconcileSuccess(entries: List<LedgerEntry>): Boolean =
        entries.any { it.status == EntryStatus.ACTIVE && it.type == EntryType.TRANSFER }

    @Deprecated("Use hasReconcileSuccess(entries)")
    fun hasSavingsOrReconcileSuccess(
        entries: List<LedgerEntry>,
        totalSettleDays: Int
    ): Boolean = hasReconcileSuccess(entries) || totalSettleDays >= 1

    /**
     * 渲染閘：shop_day_reached AND owned（[RanchBuildShopLogic.isRenderable]）。
     */
    fun mode(category: LedgerCategory, ctx: ShopContext): HotspotUnlockMode =
        if (RanchBuildShopLogic.isRenderable(category, ctx)) HotspotUnlockMode.UNLOCKED
        else HotspotUnlockMode.HIDDEN

    fun isVisible(category: LedgerCategory, ctx: ShopContext): Boolean =
        mode(category, ctx) == HotspotUnlockMode.UNLOCKED

    fun unlockedCategories(ctx: ShopContext): Set<LedgerCategory> =
        RanchBuildShopLogic.visibleCategories(ctx)

    /** 僅有 owned、無日結上下文時：未買＝隱藏（開局／測試用） */
    fun mode(category: LedgerCategory, ownedIds: Set<String>): HotspotUnlockMode =
        mode(
            category,
            ShopContext(
                totalSettleDays = if (ownedIds.isEmpty()) 0 else Int.MAX_VALUE,
                ownedIds = ownedIds,
                hasReconcileSuccess = false
            )
        )

    fun isVisible(category: LedgerCategory, ownedIds: Set<String>): Boolean =
        mode(category, ownedIds) == HotspotUnlockMode.UNLOCKED

    fun unlockedCategories(ownedIds: Set<String>): Set<LedgerCategory> =
        LedgerCategory.entries.filter { isVisible(it, ownedIds) }.toSet()

    @Deprecated("Pass ShopContext or ownedIds; schedule no longer auto-shows map objects")
    fun scheduledUnlockDay(category: LedgerCategory): Int = when (category) {
        LedgerCategory.FOOD,
        LedgerCategory.HOUSING,
        LedgerCategory.INCOME -> Int.MAX_VALUE
        LedgerCategory.DAILY -> 3
        LedgerCategory.TRANSPORT -> 5
        LedgerCategory.SAVINGS -> 7
        LedgerCategory.ENTERTAINMENT -> 10
        LedgerCategory.HEALTH -> 12
        LedgerCategory.OTHER -> 14
    }

    @Deprecated("Pass ownedIds / ShopContext")
    fun mode(
        category: LedgerCategory,
        ranchDay: Int,
        expenseEntryCount: Int = 0,
        hasSavingsOrReconcileSuccess: Boolean = false
    ): HotspotUnlockMode = mode(category, ownedIds = emptySet())

    @Deprecated("Pass ownedIds / ShopContext")
    fun isVisible(
        category: LedgerCategory,
        ranchDay: Int,
        expenseEntryCount: Int = 0,
        hasSavingsOrReconcileSuccess: Boolean = false
    ): Boolean = false

    @Deprecated("Pass ownedIds / ShopContext")
    fun unlockedCategories(
        ranchDay: Int,
        expenseEntryCount: Int = 0,
        hasSavingsOrReconcileSuccess: Boolean = false
    ): Set<LedgerCategory> = emptySet()
}
