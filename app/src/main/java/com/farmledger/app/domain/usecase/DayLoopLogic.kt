package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.DayPhase

/**
 * M4 一日生活節奏指引（純 UI／流程建議；唔改結算發獎）。
 *
 * 起床 → 勞作（種澆收餵）→ 黃昏商店（種子幣；唔入港幣帳）→ 夜晚日結 → 瞓覺過日
 * 睡覺／等待從不發成長點；日結仍固定 1 點／本地日一次（見 DailySettlementLogic）。
 */
enum class DayLoopFocus(val nameZh: String) {
    WAKE("起床"),
    WORK("勞作"),
    SHOP("商店"),
    SETTLE("日結"),
    SLEEP("瞓覺")
}

data class DayLoopGuide(
    val focus: DayLoopFocus,
    val titleZh: String,
    val hintZh: String,
    val primaryCtaZh: String
)

object DayLoopLogic {

    /**
     * @param todaySettled 今日本地日是否已成功結算（唔影響金額／筆數）
     */
    fun guide(phase: DayPhase, todaySettled: Boolean): DayLoopGuide = when (phase) {
        DayPhase.MORNING -> DayLoopGuide(
            focus = DayLoopFocus.WAKE,
            titleZh = "起床・新一日",
            hintZh = "晨光正好：去種、澆、收、餵。勞作唔發成長點。",
            primaryCtaZh = "去田裏勞作"
        )
        DayPhase.NOON -> DayLoopGuide(
            focus = DayLoopFocus.WORK,
            titleZh = "晝・勞作",
            hintZh = "繼續種澆收餵；準備好就等下一時段。",
            primaryCtaZh = "繼續勞作"
        )
        DayPhase.EVENING -> DayLoopGuide(
            focus = DayLoopFocus.SHOP,
            titleZh = "黃昏商店",
            hintZh = "種子幣買種子／飼料、賣收成；唔入真港幣帳、唔發成長點。",
            primaryCtaZh = "開商店"
        )
        DayPhase.NIGHT -> if (!todaySettled) {
            DayLoopGuide(
                focus = DayLoopFocus.SETTLE,
                titleZh = "夜晚・日結",
                hintZh = "有記（真帳／無交易日）可結算：固定 1 成長點＋1 種子幣；改帳唔重派。",
                primaryCtaZh = "去日結"
            )
        } else {
            DayLoopGuide(
                focus = DayLoopFocus.SLEEP,
                titleZh = "夜晚・瞓覺過日",
                hintZh = "今日已結清。睡覺進入翌日晨；睡覺唔發成長點。",
                primaryCtaZh = "瞓覺・下一日"
            )
        }
    }

    fun isShopPreferred(phase: DayPhase): Boolean = phase == DayPhase.EVENING

    fun isSettlePreferred(phase: DayPhase): Boolean = phase == DayPhase.NIGHT

    fun isWorkPreferred(phase: DayPhase): Boolean =
        phase == DayPhase.MORNING || phase == DayPhase.NOON

    fun isSleepPreferred(phase: DayPhase, todaySettled: Boolean): Boolean =
        phase == DayPhase.NIGHT && todaySettled

    /** 一日生活順序標籤（HUD／說明用） */
    fun loopLabelsZh(): List<String> =
        listOf("起床", "勞作", "黃昏商店", "夜結", "瞓覺過日")

    fun focusIconHint(focus: DayLoopFocus): String = when (focus) {
        DayLoopFocus.WAKE -> "晨"
        DayLoopFocus.WORK -> "種"
        DayLoopFocus.SHOP -> "買"
        DayLoopFocus.SETTLE -> "月"
        DayLoopFocus.SLEEP -> "眠"
    }
}
