package com.farmledger.app.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    /** 主世界：單一農場場景 */
    const val FARM = "farm"
    const val WEEKLY = "weekly"
    const val SETTINGS = "settings"

    // 保留舊路由字串供深層連結／測試相容；導航以農場 + overlay 為主
    // 記帳新增／編輯在農場 Ledger ModalBottomSheet 內完成，不再導向獨立全螢幕
    const val HOME = FARM
    const val LEDGER = "ledger"
    const val SETTLE = "settle"
    const val PET = "pet"
    const val DECOR = "decor"
    @Deprecated("Entry edit is nested in farm ledger sheet; not a NavHost destination")
    const val ENTRY_EDIT = "entry_edit?id={id}"

    @Deprecated("Entry edit is nested in farm ledger sheet")
    fun entryEdit(id: String? = null): String =
        if (id == null) "entry_edit?id=" else "entry_edit?id=$id"
}
