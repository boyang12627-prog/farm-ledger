package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry
import java.time.LocalDate

/**
 * 漏記長草：某分類今日未有 ACTIVE 帳 → 牧場物件旁顯示長草。
 * 九分類各自獨立；有記則收草。
 * 0.5.3-sys：按連續漏記日數堆疊草層（1–min(N,3)），唔用單層半透明章。
 */
object MissedCategoryLogic {
    const val MAX_WEED_STACK = 3
    const val LOOKBACK_DAYS = 14

    fun usedCategoriesToday(entries: List<LedgerEntry>, today: String): Set<LedgerCategory> =
        entries
            .filter { it.localDate == today && it.status == EntryStatus.ACTIVE }
            .mapNotNull { LedgerCategory.fromStorage(it.category) }
            .toSet()

    fun missedCategories(entries: List<LedgerEntry>, today: String): Set<LedgerCategory> {
        val used = usedCategoriesToday(entries, today)
        return LedgerCategory.entries.filter { it !in used }.toSet()
    }

    /** 日記殼：常用支出 chips 漏記（兼容舊顯示） */
    fun missedExpenseChips(entries: List<LedgerEntry>, today: String): List<LedgerCategory> {
        val used = usedCategoriesToday(entries, today)
        val hasAny = entries.any { it.localDate == today && it.status == EntryStatus.ACTIVE }
        return if (!hasAny) LedgerCategory.expenseChips
        else LedgerCategory.expenseChips.filter { it !in used }
    }

    /**
     * 連續漏記日數（含今日若未記）。今日有記 → 0。
     * 由 today 往回數，遇有該分類 ACTIVE 帳即停。
     */
    fun consecutiveMissDays(
        entries: List<LedgerEntry>,
        category: LedgerCategory,
        today: String,
        lookback: Int = LOOKBACK_DAYS
    ): Int {
        val start = runCatching { LocalDate.parse(today) }.getOrNull() ?: return 0
        var count = 0
        for (i in 0 until lookback) {
            val d = start.minusDays(i.toLong()).toString()
            val used = entries.any {
                it.localDate == d &&
                    it.status == EntryStatus.ACTIVE &&
                    LedgerCategory.fromStorage(it.category) == category
            }
            if (used) break
            count++
        }
        return count
    }

    /** 可見草層數：未漏＝0；漏記＝1…min(missDays, MAX_WEED_STACK) */
    fun weedStackCount(missDays: Int): Int =
        if (missDays <= 0) 0 else missDays.coerceAtMost(MAX_WEED_STACK)

    fun weedStacksByCategory(
        entries: List<LedgerEntry>,
        today: String
    ): Map<LedgerCategory, Int> =
        LedgerCategory.entries.associateWith { cat ->
            weedStackCount(consecutiveMissDays(entries, cat, today))
        }
}
