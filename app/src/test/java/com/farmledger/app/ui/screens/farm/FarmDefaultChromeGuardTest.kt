package com.farmledger.app.ui.screens.farm

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards 0.5.4b-land: default FarmScreen = landscape A2c, no day-loop chrome,
 * no permanent hotspot wood signs (A2c_landscape_label_spec).
 */
class FarmDefaultChromeGuardTest {

    private fun src(name: String): File {
        val roots = listOf(
            File("app/src/main/java/com/farmledger/app/ui/screens/farm"),
            File("src/main/java/com/farmledger/app/ui/screens/farm")
        )
        for (r in roots) {
            val f = File(r, name)
            if (f.isFile) return f
        }
        error("Cannot locate $name under ${File(".").absolutePath}")
    }

    private val forbidden = listOf(
        "物品欄",
        "選擇作物",
        "等待・入晝",
        "等待・入昏",
        "等待・入夜",
        "瞓覺過日",
        "瞓覺・下一日",
        "起床・新一日",
        "今日帳",
        "黃昏商店",
        "日結 +",
        "FarmSceneLayer",
        "LazyVerticalGrid"
    )

    @Test
    fun defaultFarmScreen_hasNoDayLoopChrome() {
        val text = src("FarmScreen.kt").readText(Charsets.UTF_8)
        for (s in forbidden) {
            assertFalse(
                "FarmScreen.kt must not contain day-loop chrome: 「$s」",
                text.contains(s)
            )
        }
        assertTrue(text.contains("RanchTopBar"))
        assertTrue(text.contains("RanchHotspotScene"))
        assertTrue(text.contains("LegacyFarmDayLoopScreen") || text.contains("舊一日循環"))
        // 週回顧／設定 row removed from farm chrome
        assertFalse("FarmScreen must not host 週回顧 button row", text.contains("週回顧"))
        assertFalse("FarmScreen must not pass HKD to top bar", text.contains("hkdMonthSummary") || text.contains("HK$"))
    }

    @Test
    fun ranchHotspotScene_noPermanentWoodSigns() {
        val text = src("RanchHotspotScene.kt").readText(Charsets.UTF_8)
        assertFalse(
            "RanchHotspotScene must not use floating_wood_sign drawable (spec §3)",
            text.contains("R.drawable.floating_wood_sign") || text.contains("floating_wood_sign")
        )
        assertTrue(text.contains("WhisperLabel") || text.contains("whisper"))
        assertTrue(text.contains("WhisperLongPressMs") || text.contains("400"))
        assertTrue(text.contains("showHotspotLabels"))
    }

    @Test
    fun legacyScreen_stillHostsOldChrome() {
        val text = src("LegacyFarmDayLoopScreen.kt").readText(Charsets.UTF_8)
        assertTrue(text.contains("物品欄"))
        assertTrue(text.contains("選擇作物"))
        assertTrue(text.contains("等待・入晝") || text.contains("等待・入昏"))
        assertTrue(text.contains("fun LegacyFarmDayLoopScreen"))
    }

    @Test
    fun farmScreen_topBarUsesRanchDayNotStreak() {
        val text = src("FarmScreen.kt").readText(Charsets.UTF_8)
        assertTrue("FarmScreen must pass ranchDay to RanchTopBar", text.contains("ranchDay"))
        assertFalse(
            "FarmScreen must not pass streakDays to RanchTopBar plaque",
            "streakDays = progress.streakDays" in text
        )
    }

    @Test
    fun ranchHotspotScene_gatesByUnlockLogic() {
        val text = src("RanchHotspotScene.kt").readText(Charsets.UTF_8)
        assertTrue(text.contains("HotspotUnlockLogic"))
        assertTrue(text.contains("ranchDay"))
        assertFalse(
            "Must not draw LOCKED_GRAY piles on map",
            text.contains("LOCKED_GRAY")
        )
    }
}
