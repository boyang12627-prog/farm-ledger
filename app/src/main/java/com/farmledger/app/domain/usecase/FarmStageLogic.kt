package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.FarmStage
import com.farmledger.app.domain.model.StageRules

/**
 * 農場階段由「累計結算日數」解鎖（非記帳筆數、非連續 streak）。
 * 門檻：3／7／14／21 → 萌芽／安家／旺場／旺場・二寵位。
 */
data class FarmStageCapabilities(
    val stage: FarmStage,
    val unlockedPlotCount: Int,
    val petFeedSlots: Int,
    val animalSlots: Int,
    val decorSlots: Int,
    val showGrassEdges: Boolean,
    val showHut: Boolean,
    val showFence: Boolean,
    val greenerDenser: Boolean,
    val smallExpansion: Boolean
)

object FarmStageLogic {

    fun stageFor(totalSettleDays: Int): FarmStage {
        val days = totalSettleDays.coerceAtLeast(0)
        return when {
            days >= StageRules.THRIVING_ANIMAL2_DAYS -> FarmStage.THRIVING
            days >= StageRules.THRIVING_DAYS -> FarmStage.THRIVING
            days >= StageRules.HOMESTEAD_DAYS -> FarmStage.HOMESTEAD
            days >= StageRules.SPROUT_DAYS -> FarmStage.SPROUT
            else -> FarmStage.BARREN
        }
    }

    fun capabilities(totalSettleDays: Int): FarmStageCapabilities {
        val days = totalSettleDays.coerceAtLeast(0)
        val stage = stageFor(days)
        return when (stage) {
            FarmStage.BARREN -> FarmStageCapabilities(
                stage = stage,
                unlockedPlotCount = 2,
                petFeedSlots = 0,
                animalSlots = 0,
                decorSlots = 0,
                showGrassEdges = false,
                showHut = false,
                showFence = false,
                greenerDenser = false,
                smallExpansion = false
            )
            FarmStage.SPROUT -> FarmStageCapabilities(
                stage = stage,
                unlockedPlotCount = 6,
                petFeedSlots = 1,
                animalSlots = 1,
                decorSlots = 0,
                showGrassEdges = true,
                showHut = false,
                showFence = false,
                greenerDenser = false,
                smallExpansion = false
            )
            FarmStage.HOMESTEAD -> FarmStageCapabilities(
                stage = stage,
                unlockedPlotCount = 6,
                petFeedSlots = 1,
                animalSlots = 1,
                decorSlots = 1,
                showGrassEdges = true,
                showHut = true,
                showFence = true,
                greenerDenser = false,
                smallExpansion = false
            )
            FarmStage.THRIVING -> FarmStageCapabilities(
                stage = stage,
                unlockedPlotCount = 6,
                petFeedSlots = 1,
                animalSlots = if (days >= StageRules.THRIVING_ANIMAL2_DAYS) 2 else 1,
                decorSlots = 1,
                showGrassEdges = true,
                showHut = true,
                showFence = true,
                greenerDenser = true,
                smallExpansion = true
            )
        }
    }

    fun isPlotUnlocked(totalSettleDays: Int, plotIndex: Int): Boolean {
        if (plotIndex < 0) return false
        return plotIndex < capabilities(totalSettleDays).unlockedPlotCount
    }

    fun nextUnlock(totalSettleDays: Int): Pair<Int, String>? {
        val days = totalSettleDays.coerceAtLeast(0)
        val thresholds = listOf(
            StageRules.SPROUT_DAYS to FarmStage.SPROUT.nameZh,
            StageRules.HOMESTEAD_DAYS to FarmStage.HOMESTEAD.nameZh,
            StageRules.THRIVING_DAYS to FarmStage.THRIVING.nameZh,
            StageRules.THRIVING_ANIMAL2_DAYS to "第二動物欄"
        )
        return thresholds.firstOrNull { days < it.first }
    }
}
