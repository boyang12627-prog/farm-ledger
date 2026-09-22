package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.DayPhase
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DayLoopLogicTest {

    @Test
    fun morning_isWakeThenWorkPreferred() {
        val g = DayLoopLogic.guide(DayPhase.MORNING, todaySettled = false)
        assertThat(g.focus).isEqualTo(DayLoopFocus.WAKE)
        assertThat(g.titleZh).contains("起床")
        assertThat(DayLoopLogic.isWorkPreferred(DayPhase.MORNING)).isTrue()
        assertThat(DayLoopLogic.isShopPreferred(DayPhase.MORNING)).isFalse()
    }

    @Test
    fun noon_isWork() {
        val g = DayLoopLogic.guide(DayPhase.NOON, todaySettled = false)
        assertThat(g.focus).isEqualTo(DayLoopFocus.WORK)
        assertThat(g.hintZh).contains("種")
        assertThat(DayLoopLogic.isWorkPreferred(DayPhase.NOON)).isTrue()
    }

    @Test
    fun evening_prefersShop_notGrowthAward() {
        val g = DayLoopLogic.guide(DayPhase.EVENING, todaySettled = false)
        assertThat(g.focus).isEqualTo(DayLoopFocus.SHOP)
        assertThat(g.titleZh).contains("黃昏")
        assertThat(g.hintZh).contains("唔發成長點")
        assertThat(DayLoopLogic.isShopPreferred(DayPhase.EVENING)).isTrue()
        assertThat(DayLoopLogic.isSettlePreferred(DayPhase.EVENING)).isFalse()
    }

    @Test
    fun night_unsettled_prefersSettle_fixedOnePointCopy() {
        val g = DayLoopLogic.guide(DayPhase.NIGHT, todaySettled = false)
        assertThat(g.focus).isEqualTo(DayLoopFocus.SETTLE)
        assertThat(g.hintZh).contains("1")
        assertThat(g.hintZh).contains("唔重派")
        assertThat(DayLoopLogic.isSettlePreferred(DayPhase.NIGHT)).isTrue()
        assertThat(DayLoopLogic.isSleepPreferred(DayPhase.NIGHT, todaySettled = false)).isFalse()
    }

    @Test
    fun night_settled_prefersSleep_noGrowthFromSleep() {
        val g = DayLoopLogic.guide(DayPhase.NIGHT, todaySettled = true)
        assertThat(g.focus).isEqualTo(DayLoopFocus.SLEEP)
        assertThat(g.hintZh).contains("唔發成長點")
        assertThat(DayLoopLogic.isSleepPreferred(DayPhase.NIGHT, todaySettled = true)).isTrue()
    }

    @Test
    fun continuousDayLabels_coverFullLoop() {
        assertThat(DayLoopLogic.loopLabelsZh())
            .containsExactly("起床", "勞作", "黃昏商店", "夜結", "瞓覺過日")
            .inOrder()
    }

    @Test
    fun waitAndSleep_stillOrthogonalToRewards_viaDayPhaseLogic() {
        // 指引層唔發獎；推進仍走 DayPhaseLogic（已有測試證明睡覺唔改 gameDay 以外獎勵欄）
        val night = com.farmledger.app.domain.model.GameDayState(
            gameDay = 2,
            phase = DayPhase.NIGHT,
            lastWallClockEpochMs = 1L,
            updatedAtEpochMs = 1L
        )
        val slept = DayPhaseLogic.sleepToNextDay(night, clockPaused = false, nowEpochMs = 2L, todaySettled = true)
        assertThat(slept.slept).isTrue()
        assertThat(slept.state.phase).isEqualTo(DayPhase.MORNING)
        assertThat(slept.messageZh).contains("不發成長點")
        val wakeGuide = DayLoopLogic.guide(slept.state.phase, todaySettled = false)
        assertThat(wakeGuide.focus).isEqualTo(DayLoopFocus.WAKE)
    }
}
