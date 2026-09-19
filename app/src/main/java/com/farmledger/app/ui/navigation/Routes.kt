package com.farmledger.app.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val LEDGER = "ledger"
    const val ENTRY_EDIT = "entry_edit?id={id}"
    const val SETTLE = "settle"
    const val FARM = "farm"
    const val PET = "pet"
    const val DECOR = "decor"
    const val WEEKLY = "weekly"
    const val SETTINGS = "settings"

    fun entryEdit(id: String? = null): String =
        if (id == null) "entry_edit?id=" else "entry_edit?id=$id"
}
