package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry

/**
 * 漏記長草：某分類今日未有 ACTIVE 帳 → 牧場物件旁顯示長草。
 * 九分類各自獨立；有記則收草。
 */
object MissedCategoryLogic {
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
}
