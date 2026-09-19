package com.farmledger.app.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    /** 主世界：單一農場場景 */
    const val FARM = "farm"
    const val ENTRY_EDIT = "entry_edit?id={id}"
    const val WEEKLY = "weekly"
    const val SETTINGS = "settings"

    // 保留舊路由字串供深層連結／測試相容；導航以農場 + overlay 為主
    const val HOME = FARM
    const val LEDGER = "ledger"
    const val SETTLE = "settle"
    const val PET = "pet"
    const val DECOR = "decor"

    fun entryEdit(id: String? = null): String =
        if (id == null) "entry_edit?id=" else "entry_edit?id=$id"
}
