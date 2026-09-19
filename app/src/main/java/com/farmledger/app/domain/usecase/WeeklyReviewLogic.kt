package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.RewardRules

data class WeeklyReviewResult(
    val progress: PlayerProgress,
    val awardedPoints: Int,
    val messageZh: String
)

object WeeklyReviewLogic {

    /** weekId 例如 2026-W38 */
    fun claim(
        progress: PlayerProgress,
        weekId: String,
        settledDaysThisWeek: Int
    ): WeeklyReviewResult {
        if (progress.clockPaused) {
            return WeeklyReviewResult(progress, 0, "時鐘倒退中，週回顧獎勵暫停。")
        }
        if (progress.weeklyReviewClaimedWeekId == weekId) {
            return WeeklyReviewResult(progress, 0, "本週回顧獎勵已領取。")
        }
        // 每結算一日給 1 點，上限 WEEKLY_REVIEW_CAP_POINTS
        val raw = settledDaysThisWeek.coerceAtLeast(0)
        val points = raw.coerceAtMost(RewardRules.WEEKLY_REVIEW_CAP_POINTS)
        if (points <= 0) {
            return WeeklyReviewResult(progress, 0, "本週尚無結算紀錄，無法領取。")
        }
        val updated = progress.copy(
            growthPoints = progress.growthPoints + points,
            weeklyReviewClaimedWeekId = weekId
        )
        return WeeklyReviewResult(
            updated,
            points,
            "週回顧完成！獲得 $points 成長點（上限 ${RewardRules.WEEKLY_REVIEW_CAP_POINTS}）。"
        )
    }
}
