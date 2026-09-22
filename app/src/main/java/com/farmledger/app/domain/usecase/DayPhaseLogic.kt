package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.DayPhase
import com.farmledger.app.domain.model.FarmStage
import com.farmledger.app.domain.model.GameDayState

/**
 * 日循環狀態機：
 * - 晨 → 晝 → 昏 → 夜（等待推进）
 * - 夜晚睡覺 → 翌日晨、gameDay +1
 * - 時鐘倒退／clockPaused：不推進日與時段（亦不發獎；發獎由 DailySettlementLogic）
 * - 睡覺／等待從不發成長點
 */
data class DayPhaseResult(
    val state: GameDayState,
    val advanced: Boolean,
    val slept: Boolean,
    val messageZh: String
)

object DayPhaseLogic {

    fun waitNextPhase(
        state: GameDayState,
        clockPaused: Boolean,
        nowEpochMs: Long,
        farmStage: FarmStage = FarmStage.BARREN
    ): DayPhaseResult {
        if (clockPaused) {
            return DayPhaseResult(
                state = state,
                advanced = false,
                slept = false,
                messageZh = "時間倒退中，時段暫停推進。"
            )
        }
        val next = state.phase.nextOrNull()
            ?: return DayPhaseResult(
                state = state,
                advanced = false,
                slept = false,
                messageZh = "已是夜晚，請睡覺進入下一日。"
            )
        val updated = state.copy(
            phase = next,
            farmStageHint = farmStage.name,
            lastWallClockEpochMs = nowEpochMs,
            updatedAtEpochMs = nowEpochMs
        )
        return DayPhaseResult(
            state = updated,
            advanced = true,
            slept = false,
            messageZh = "時段：${state.phase.nameZh} → ${next.nameZh}"
        )
    }

    fun sleepToNextDay(
        state: GameDayState,
        clockPaused: Boolean,
        nowEpochMs: Long,
        farmStage: FarmStage = FarmStage.BARREN
    ): DayPhaseResult {
        if (clockPaused) {
            return DayPhaseResult(
                state = state,
                advanced = false,
                slept = false,
                messageZh = "時間倒退中，無法睡覺過日。"
            )
        }
        if (state.phase != DayPhase.NIGHT) {
            return DayPhaseResult(
                state = state,
                advanced = false,
                slept = false,
                messageZh = "只能在夜晚睡覺。現在是「${state.phase.nameZh}」。"
            )
        }
        val updated = state.copy(
            gameDay = state.gameDay + 1,
            phase = DayPhase.MORNING,
            farmStageHint = farmStage.name,
            lastWallClockEpochMs = nowEpochMs,
            updatedAtEpochMs = nowEpochMs
        )
        return DayPhaseResult(
            state = updated,
            advanced = true,
            slept = true,
            messageZh = "晚安！瞓覺過日 → 第 ${updated.gameDay} 日起床（睡覺不發成長點）。"
        )
    }

    /**
     * 牆鐘倒退偵測：若 now < lastWallClock（明顯倒退），呼叫端應設 clockPaused。
     * 回傳 true 表示偵測到倒退。
     */
    fun isWallClockRegression(state: GameDayState, nowEpochMs: Long): Boolean {
        val last = state.lastWallClockEpochMs
        if (last <= 0L) return false
        // 允許小幅誤差；明顯倒退（> 1 分鐘）才算
        return nowEpochMs + 60_000L < last
    }

    fun touchWallClock(state: GameDayState, nowEpochMs: Long, farmStage: FarmStage): GameDayState {
        if (state.lastWallClockEpochMs <= 0L) {
            return state.copy(
                lastWallClockEpochMs = nowEpochMs,
                updatedAtEpochMs = nowEpochMs,
                farmStageHint = farmStage.name
            )
        }
        return state.copy(
            lastWallClockEpochMs = maxOf(state.lastWallClockEpochMs, nowEpochMs),
            farmStageHint = farmStage.name,
            updatedAtEpochMs = nowEpochMs
        )
    }

    fun phaseTintArgb(phase: DayPhase): Long = when (phase) {
        DayPhase.MORNING -> 0x33FFD27A // 金黃輕 overlay
        DayPhase.NOON -> 0x00000000     // 標準無 tint
        DayPhase.EVENING -> 0x44C46B3A // 橙紫
        DayPhase.NIGHT -> 0x66202A5A   // 藍紫
    }
}
