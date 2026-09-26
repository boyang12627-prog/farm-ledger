package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory

/**
 * 牧場熱區／分類預填入帳類型。
 * 儲蓄（撲滿）→ 轉帳（或支出入儲蓄戶），**唔好**預填收入。
 */
object EntryPrefillLogic {
    fun entryTypeForCategory(category: LedgerCategory): EntryType = when (category) {
        LedgerCategory.INCOME -> EntryType.INCOME
        LedgerCategory.SAVINGS -> EntryType.TRANSFER
        else -> EntryType.EXPENSE
    }
}
