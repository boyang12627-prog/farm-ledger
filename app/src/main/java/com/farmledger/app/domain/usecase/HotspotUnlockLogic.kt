package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry

/**
 * 熱區地圖捷徑可見性（≠入帳分類可用性）。
 *
 * **鐵則**：未解鎖＝[HotspotUnlockMode.HIDDEN]（唔畫、唔灰桩、無大木牌牆）；
 * 分類仍可經底欄「入帳」完整選。
 *
 * 節奏對齊 `docs/art/a2c/unlock/day1_sparse.json`（A2c-day1-sparse）。
 * 策劃正式表到齊前以此為準；改表請同步 json＋本 object。
 */
enum class HotspotUnlockMode {
    /** 唔畫物件／雜草／hit target */
    HIDDEN,
    /** 可見可點（扑满仍受帳戶閘 [HotspotInteractionLogic.isEnabled]） */
    UNLOCKED
}

object HotspotUnlockLogic {

    /** json id → [LedgerCategory]（day1_sparse.json） */
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

    /**
     * 日程解鎖日（1-based ranchDay＝gameDay）。
     * D1 home/food/income｜D3 daily｜D5 transit｜D7 save｜D10 fun｜D12 health｜D14 other
     */
    fun scheduledUnlockDay(category: LedgerCategory): Int = when (category) {
        LedgerCategory.FOOD,
        LedgerCategory.HOUSING,
        LedgerCategory.INCOME -> 1
        LedgerCategory.DAILY -> 3
        LedgerCategory.TRANSPORT -> 5
        LedgerCategory.SAVINGS -> 7
        LedgerCategory.ENTERTAINMENT -> 10
        LedgerCategory.HEALTH -> 12
        LedgerCategory.OTHER -> 14
    }

    /** ACTIVE 支出筆數（提前解鎖日用木箱） */
    fun activeExpenseCount(entries: List<LedgerEntry>): Int =
        entries.count { it.status == EntryStatus.ACTIVE && it.type == EntryType.EXPENSE }

    /**
     * 首次儲蓄／對帳成功：有 ACTIVE 轉帳（扑满／儲蓄），或已至少結算過一日。
     * 用於提前解鎖 save。
     */
    fun hasSavingsOrReconcileSuccess(
        entries: List<LedgerEntry>,
        totalSettleDays: Int
    ): Boolean =
        totalSettleDays >= 1 ||
            entries.any { it.status == EntryStatus.ACTIVE && it.type == EntryType.TRANSFER }

    /**
     * @param ranchDay 牧場／遊戲日（1-based）；唔係連續記帳 streak
     * @param expenseEntryCount ACTIVE 支出筆數（≥3 → daily 提前）
     * @param hasSavingsOrReconcileSuccess 首次儲蓄／對帳 → save 提前
     */
    fun mode(
        category: LedgerCategory,
        ranchDay: Int,
        expenseEntryCount: Int = 0,
        hasSavingsOrReconcileSuccess: Boolean = false
    ): HotspotUnlockMode {
        val dayOk = ranchDay >= scheduledUnlockDay(category)
        val early = when (category) {
            LedgerCategory.DAILY -> expenseEntryCount >= 3
            LedgerCategory.SAVINGS -> hasSavingsOrReconcileSuccess
            else -> false
        }
        return if (dayOk || early) HotspotUnlockMode.UNLOCKED else HotspotUnlockMode.HIDDEN
    }

    fun isVisible(
        category: LedgerCategory,
        ranchDay: Int,
        expenseEntryCount: Int = 0,
        hasSavingsOrReconcileSuccess: Boolean = false
    ): Boolean =
        mode(category, ranchDay, expenseEntryCount, hasSavingsOrReconcileSuccess) ==
            HotspotUnlockMode.UNLOCKED

    fun unlockedCategories(
        ranchDay: Int,
        expenseEntryCount: Int = 0,
        hasSavingsOrReconcileSuccess: Boolean = false
    ): Set<LedgerCategory> =
        LedgerCategory.entries.filter {
            isVisible(it, ranchDay, expenseEntryCount, hasSavingsOrReconcileSuccess)
        }.toSet()
}
