package com.farmledger.app.ui.screens.farm

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the 0.5.4-fix contract: default FarmScreen must NOT compose the old
 * day-loop chrome (起床橫幅／物品欄／選擇作物／等待入晝／瞓覺). That UI lives only in
 * [LegacyFarmDayLoopScreen].
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
    }

    @Test
    fun legacyScreen_stillHostsOldChrome() {
        val text = src("LegacyFarmDayLoopScreen.kt").readText(Charsets.UTF_8)
        assertTrue(text.contains("物品欄"))
        assertTrue(text.contains("選擇作物"))
        assertTrue(text.contains("等待・入晝") || text.contains("等待・入昏"))
        assertTrue(text.contains("fun LegacyFarmDayLoopScreen"))
    }
}
