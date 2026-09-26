package com.farmledger.app.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    /** 主世界：牧場場景 */
    const val FARM = "farm"
    /** M5：真港幣極速入帳 */
    const val ENTRY = "entry"
    /** M5：跨日帳簿 */
    const val LEDGER = "ledger"
    /** M5：日記殼 */
    const val DIARY = "diary"
    const val WEEKLY = "weekly"
    const val SETTINGS = "settings"

    const val HOME = FARM
    const val SETTLE = "settle"
    const val PET = "pet"
    const val DECOR = "decor"
    @Deprecated("Entry edit nested in ledger / quick entry")
    const val ENTRY_EDIT = "entry_edit?id={id}"

    @Deprecated("Entry edit nested in ledger")
    fun entryEdit(id: String? = null): String =
        if (id == null) "entry_edit?id=" else "entry_edit?id=$id"
}
