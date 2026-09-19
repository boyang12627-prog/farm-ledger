package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.RewardRules
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeeklyReviewLogicTest {

    @Test
    fun weeklyReward_capped() {
        val p = PlayerProgress(growthPoints = 0)
        val r = WeeklyReviewLogic.claim(p, "2026-W38", settledDaysThisWeek = 7)
        assertThat(r.awardedPoints).isEqualTo(RewardRules.WEEKLY_REVIEW_CAP_POINTS)
        assertThat(r.progress.growthPoints).isEqualTo(RewardRules.WEEKLY_REVIEW_CAP_POINTS)
    }

    @Test
    fun weeklyReward_notDoubleClaim() {
        val p = PlayerProgress(weeklyReviewClaimedWeekId = "2026-W38", growthPoints = 3)
        val r = WeeklyReviewLogic.claim(p, "2026-W38", settledDaysThisWeek = 5)
        assertThat(r.awardedPoints).isEqualTo(0)
        assertThat(r.progress.growthPoints).isEqualTo(3)
    }
}
