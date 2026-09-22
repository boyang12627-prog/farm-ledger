package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.DayPhase
import com.farmledger.app.domain.model.FarmStage
import com.farmledger.app.domain.model.GameDayState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DayPhaseLogicTest {

    private val base = GameDayState(
        gameDay = 1,
        phase = DayPhase.MORNING,
        farmStageHint = FarmStage.BARREN.name,
        lastWallClockEpochMs = 1_000L,
        updatedAtEpochMs = 1_000L
    )

    @Test
    fun wait_advancesMorningToNoonToEveningToNight() {
        var s = base
        val r1 = DayPhaseLogic.waitNextPhase(s, clockPaused = false, nowEpochMs = 2_000L)
        assertThat(r1.advanced).isTrue()
        assertThat(r1.state.phase).isEqualTo(DayPhase.NOON)
        s = r1.state

        val r2 = DayPhaseLogic.waitNextPhase(s, false, 3_000L)
        assertThat(r2.state.phase).isEqualTo(DayPhase.EVENING)
        s = r2.state

        val r3 = DayPhaseLogic.waitNextPhase(s, false, 4_000L)
        assertThat(r3.state.phase).isEqualTo(DayPhase.NIGHT)
        s = r3.state

        val r4 = DayPhaseLogic.waitNextPhase(s, false, 5_000L)
        assertThat(r4.advanced).isFalse()
        assertThat(r4.messageZh).contains("睡覺")
    }

    @Test
    fun sleep_onlyAtNight_advancesDayToMorning() {
        val night = base.copy(phase = DayPhase.NIGHT, gameDay = 3)
        val blocked = DayPhaseLogic.sleepToNextDay(base, false, 9_000L)
        assertThat(blocked.advanced).isFalse()
        assertThat(blocked.slept).isFalse()

        val ok = DayPhaseLogic.sleepToNextDay(night, false, 10_000L)
        assertThat(ok.advanced).isTrue()
        assertThat(ok.slept).isTrue()
        assertThat(ok.state.gameDay).isEqualTo(4)
        assertThat(ok.state.phase).isEqualTo(DayPhase.MORNING)
        // 睡覺不發成長點：狀態機不含 growthPoints 欄位
        assertThat(ok.messageZh).contains("不發成長點")
    }

    @Test
    fun clockPaused_blocksWaitAndSleep() {
        val night = base.copy(phase = DayPhase.NIGHT)
        val w = DayPhaseLogic.waitNextPhase(base, clockPaused = true, nowEpochMs = 1L)
        assertThat(w.advanced).isFalse()
        assertThat(w.messageZh).contains("倒退")

        val s = DayPhaseLogic.sleepToNextDay(night, clockPaused = true, nowEpochMs = 1L)
        assertThat(s.advanced).isFalse()
        assertThat(s.slept).isFalse()
        assertThat(s.state.gameDay).isEqualTo(1)
    }

    @Test
    fun wallClockRegression_detectedWhenNowMuchEarlier() {
        val state = base.copy(lastWallClockEpochMs = 1_000_000L)
        assertThat(DayPhaseLogic.isWallClockRegression(state, nowEpochMs = 100L)).isTrue()
        assertThat(DayPhaseLogic.isWallClockRegression(state, nowEpochMs = 1_000_000L)).isFalse()
        assertThat(DayPhaseLogic.isWallClockRegression(state.copy(lastWallClockEpochMs = 0L), 1L)).isFalse()
    }

    @Test
    fun wait_doesNotChangeGameDay() {
        val r = DayPhaseLogic.waitNextPhase(base, false, 50L, FarmStage.SPROUT)
        assertThat(r.state.gameDay).isEqualTo(1)
        assertThat(r.state.farmStageHint).isEqualTo(FarmStage.SPROUT.name)
        assertThat(r.slept).isFalse()
    }

    @Test
    fun phaseTintArgb_matchesM1ArtDocs() {
        assertThat(DayPhaseLogic.phaseTintArgb(DayPhase.MORNING)).isEqualTo(0x33FFD27AL)
        assertThat(DayPhaseLogic.phaseTintArgb(DayPhase.NOON)).isEqualTo(0x00000000L)
        assertThat(DayPhaseLogic.phaseTintArgb(DayPhase.EVENING)).isEqualTo(0x44C46B3AL)
        assertThat(DayPhaseLogic.phaseTintArgb(DayPhase.NIGHT)).isEqualTo(0x66202A5AL)
    }
}
