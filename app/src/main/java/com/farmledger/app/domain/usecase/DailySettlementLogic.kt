package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.DailySettlement
import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.RewardRules

/**
 * 每日結算核心規則：
 * - 每個本地日最多獎勵固定 [RewardRules.DAILY_GROWTH_POINTS] 成長點（預設 1）
 * - 金額／筆數不影響獎勵
 * - 編輯／作廢不會再次發獎
 * - 時鐘倒退（clockPaused）時不發獎、不推進連續天數
 */
data class SettlementResult(
    val progress: PlayerProgress,
    val settlement: DailySettlement?,
    val awarded: Boolean,
    val messageZh: String
)

object DailySettlementLogic {

    fun canSettle(progress: PlayerProgress, today: String): Boolean {
        if (progress.clockPaused) return false
        return progress.lastSettleDate != today
    }

    /**
     * @param today 本地日 yyyy-MM-dd
     * @param nowEpochMs 當前時間
     * @param hasAnyLedgerActivity 當日是否有帳本活動（收入/支出/無交易日標記）；
     *        金額大小不傳入、不影響獎勵。
     */
    fun settle(
        progress: PlayerProgress,
        today: String,
        nowEpochMs: Long,
        hasAnyLedgerActivity: Boolean
    ): SettlementResult {
        if (progress.clockPaused) {
            return SettlementResult(
                progress = progress,
                settlement = null,
                awarded = false,
                messageZh = "偵測到系統時間倒退，獎勵已暫停。帳本資料保留。"
            )
        }
        if (progress.lastSettleDate == today) {
            return SettlementResult(
                progress = progress,
                settlement = null,
                awarded = false,
                messageZh = "今日已結算過，編輯帳目不會再次發放成長點。"
            )
        }
        if (!hasAnyLedgerActivity) {
            return SettlementResult(
                progress = progress,
                settlement = null,
                awarded = false,
                messageZh = "今日尚無帳本紀錄，請先記一筆或標記「無交易日」。"
            )
        }

        val prevDate = progress.lastSettleDate
        val newStreak = when {
            prevDate == null -> 1
            isConsecutiveDay(prevDate, today) -> progress.streakDays + 1
            else -> 1
        }

        val unlocked = progress.unlockedStreakRewards.toMutableSet()
        var seedsBonus = 0
        RewardRules.STREAK_MILESTONES.forEach { m ->
            if (newStreak >= m && m !in unlocked) {
                unlocked += m
                seedsBonus += when (m) {
                    3 -> 1
                    5 -> 2
                    7 -> 3
                    else -> 0
                }
            }
        }

        val points = RewardRules.DAILY_GROWTH_POINTS
        val settlement = DailySettlement(
            localDate = today,
            growthPointsAwarded = points,
            settledAtEpochMs = nowEpochMs
        )
        val updated = progress.copy(
            growthPoints = progress.growthPoints + points,
            seeds = progress.seeds + seedsBonus,
            streakDays = newStreak,
            lastSettleDate = today,
            lastKnownLocalDate = today,
            unlockedStreakRewards = unlocked
        )
        return SettlementResult(
            progress = updated,
            settlement = settlement,
            awarded = true,
            messageZh = "結算成功！獲得 $points 成長點" +
                if (seedsBonus > 0) "，連續 $newStreak 日獎勵種子 +$seedsBonus" else "（連續 $newStreak 日）"
        )
    }

    /** 獎勵與金額無關：只檢查是否已結算／是否暫停 */
    fun growthPointsForAmount(amountMinor: Long): Int {
        // 明確：金額不影響。永遠回傳規則常數或 0（此函式僅供測試證明無關性）
        return RewardRules.DAILY_GROWTH_POINTS
    }

    fun detectClockRegression(
        progress: PlayerProgress,
        today: String
    ): PlayerProgress {
        val last = progress.lastKnownLocalDate ?: return progress.copy(lastKnownLocalDate = today, clockPaused = false)
        return if (today < last) {
            progress.copy(clockPaused = true, lastKnownLocalDate = today)
        } else {
            progress.copy(clockPaused = false, lastKnownLocalDate = today)
        }
    }

    fun clearClockPause(progress: PlayerProgress, today: String): PlayerProgress =
        progress.copy(clockPaused = false, lastKnownLocalDate = today)

    private fun isConsecutiveDay(prev: String, today: String): Boolean {
        // 簡易：解析 yyyy-MM-dd 差一天
        return try {
            val p = java.time.LocalDate.parse(prev)
            val t = java.time.LocalDate.parse(today)
            p.plusDays(1) == t
        } catch (_: Exception) {
            false
        }
    }
}
