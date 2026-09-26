package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.RewardRules
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DailySettlementLogicTest {

    private val base = PlayerProgress(
        growthPoints = 0,
        seeds = 3,
        streakDays = 0,
        lastSettleDate = null,
        lastKnownLocalDate = "2026-09-14",
        clockPaused = false
    )

    @Test
    fun settle_awardsFixedOnePoint_oncePerDay() {
        val r1 = DailySettlementLogic.settle(base, "2026-09-15", 1_000L, hasAnyLedgerActivity = true)
        assertThat(r1.awarded).isTrue()
        assertThat(r1.settlement!!.growthPointsAwarded).isEqualTo(RewardRules.DAILY_GROWTH_POINTS)
        assertThat(r1.progress.growthPoints).isEqualTo(1)
        assertThat(r1.progress.seedCoins).isEqualTo(base.seedCoins + RewardRules.DAILY_SEED_COINS)
        assertThat(r1.progress.lastSettleDate).isEqualTo("2026-09-15")

        // 同日再結算 → 不發獎（防雙重）；種子幣亦不重派
        val r2 = DailySettlementLogic.settle(r1.progress, "2026-09-15", 2_000L, hasAnyLedgerActivity = true)
        assertThat(r2.awarded).isFalse()
        assertThat(r2.progress.growthPoints).isEqualTo(1)
        assertThat(r2.progress.seedCoins).isEqualTo(r1.progress.seedCoins)
    }

    @Test
    fun editDoesNotReAward_sameDayAlreadySettled() {
        val settled = base.copy(lastSettleDate = "2026-09-15", growthPoints = 1, streakDays = 1)
        // 模擬「編輯後再按結算」
        val r = DailySettlementLogic.settle(settled, "2026-09-15", 3_000L, hasAnyLedgerActivity = true)
        assertThat(r.awarded).isFalse()
        assertThat(r.messageZh).contains("已結算")
        assertThat(r.progress.growthPoints).isEqualTo(1)
    }

    @Test
    fun amountDoesNotAffectRewards() {
        // 金額不傳入 settle；growthPointsForAmount 恆為常數
        assertThat(DailySettlementLogic.growthPointsForAmount(0)).isEqualTo(1)
        assertThat(DailySettlementLogic.growthPointsForAmount(1)).isEqualTo(1)
        assertThat(DailySettlementLogic.growthPointsForAmount(999_999_99)).isEqualTo(1)
        assertThat(DailySettlementLogic.growthPointsForAmount(-50)).isEqualTo(1)

        val small = DailySettlementLogic.settle(base, "2026-09-15", 1L, hasAnyLedgerActivity = true)
        val largeBase = base.copy() // 相同 progress
        val large = DailySettlementLogic.settle(largeBase, "2026-09-15", 1L, hasAnyLedgerActivity = true)
        assertThat(small.settlement!!.growthPointsAwarded)
            .isEqualTo(large.settlement!!.growthPointsAwarded)
        assertThat(small.progress.growthPoints).isEqualTo(large.progress.growthPoints)
    }

    @Test
    fun noActivity_noAward() {
        val r = DailySettlementLogic.settle(base, "2026-09-15", 1L, hasAnyLedgerActivity = false)
        assertThat(r.awarded).isFalse()
        assertThat(r.progress.growthPoints).isEqualTo(0)
    }

    @Test
    fun clockPaused_pausesRewards_keepsProgress() {
        val paused = base.copy(clockPaused = true, growthPoints = 5)
        val r = DailySettlementLogic.settle(paused, "2026-09-15", 1L, hasAnyLedgerActivity = true)
        assertThat(r.awarded).isFalse()
        assertThat(r.progress.growthPoints).isEqualTo(5)
        assertThat(r.messageZh).contains("倒退")
    }

    @Test
    fun detectClockRegression_setsPause() {
        val p = base.copy(lastKnownLocalDate = "2026-09-15")
        val updated = DailySettlementLogic.detectClockRegression(p, "2026-09-14")
        assertThat(updated.clockPaused).isTrue()
    }

    @Test
    fun streakMilestones_unlockSeeds() {
        val day2 = base.copy(lastSettleDate = "2026-09-13", streakDays = 2, lastKnownLocalDate = "2026-09-13")
        val r = DailySettlementLogic.settle(day2, "2026-09-14", 1L, hasAnyLedgerActivity = true)
        assertThat(r.awarded).isTrue()
        assertThat(r.progress.streakDays).isEqualTo(3)
        assertThat(r.progress.unlockedStreakRewards).contains(3)
        assertThat(r.progress.seeds).isGreaterThan(day2.seeds)
    }
}
