package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry

/**
 * 熱區四態（閘門）：idle／可點／按下／禁用。
 * 「今日有記」只做徽章／微光，唔取代可點／禁用語意。
 * 撲滿＝轉帳；無足夠帳戶可轉時扑满 disabled。
 */
enum class HotspotVisualState {
    IDLE,
    CLICKABLE,
    PRESSED,
    DISABLED
}

object HotspotInteractionLogic {
    /** 撲滿需要 ≥2 個可用帳戶先可轉帳 */
    fun isSavingsTransferEnabled(activeAccountCount: Int): Boolean = activeAccountCount >= 2

    fun isEnabled(category: LedgerCategory, activeAccountCount: Int): Boolean =
        when (category) {
            LedgerCategory.SAVINGS -> isSavingsTransferEnabled(activeAccountCount)
            else -> true
        }

    /**
     * 靜態視覺態（未按住時）：禁用 → DISABLED；其餘預設 IDLE（可點）。
     * PRESSED 由 Compose 交互覆寫；CLICKABLE 可用於強調可點（例如 hover／焦點）。
     */
    fun restingState(category: LedgerCategory, activeAccountCount: Int): HotspotVisualState =
        if (isEnabled(category, activeAccountCount)) HotspotVisualState.IDLE
        else HotspotVisualState.DISABLED

    fun visualState(
        category: LedgerCategory,
        activeAccountCount: Int,
        pressed: Boolean
    ): HotspotVisualState = when {
        !isEnabled(category, activeAccountCount) -> HotspotVisualState.DISABLED
        pressed -> HotspotVisualState.PRESSED
        else -> HotspotVisualState.IDLE
    }

    /** 浮標短 label：撲滿顯示「轉帳」，其餘分類名 */
    fun floatingLabel(category: LedgerCategory): String =
        if (category == LedgerCategory.SAVINGS) "轉帳" else category.nameZh

    /** 木名牌主行：分類名＋牧場物件 */
    fun nameplateTitle(category: LedgerCategory): String =
        "${category.nameZh}・${category.farmObjectZh}"

    /** 最近一筆摘要（同分類、ACTIVE、最新） */
    fun latestEntrySummary(
        entries: List<LedgerEntry>,
        category: LedgerCategory
    ): String? {
        val hit = entries
            .filter {
                it.status == EntryStatus.ACTIVE &&
                    LedgerCategory.fromStorage(it.category) == category
            }
            .maxByOrNull { it.updatedAtEpochMs }
            ?: return null
        val major = hit.amountMinor / 100.0
        val note = hit.note.trim().take(12)
        val amt = "HK$%.2f".format(major)
        return if (note.isNotEmpty()) "$amt · $note" else "$amt · ${hit.localDate}"
    }

    /** 今日有記 → 顯示「active」徽章／微光（唔影響可點） */
    fun loggedTodayBadge(loggedToday: Set<LedgerCategory>, category: LedgerCategory): Boolean =
        category in loggedToday
}
