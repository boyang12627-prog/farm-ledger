package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.CropTimers
import com.farmledger.app.domain.model.GrowthSpendCosts
import com.farmledger.app.domain.model.StageRules
import com.farmledger.app.domain.model.PetState
import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.Plot
import com.farmledger.app.domain.model.PlotState

data class PlantResult(val plots: List<Plot>, val progress: PlayerProgress, val ok: Boolean, val msg: String)
data class HarvestResult(val plots: List<Plot>, val progress: PlayerProgress, val ok: Boolean, val msg: String)
data class SpendResult(val progress: PlayerProgress, val pet: PetState? = null, val ok: Boolean, val msg: String)

object FarmLogic {

    fun defaultPlots(): List<Plot> = (0 until 6).map { Plot(index = it) }

    fun refreshPlots(plots: List<Plot>, now: Long, clockPaused: Boolean): List<Plot> {
        if (clockPaused) return plots
        return plots.map { p ->
            if (p.state == PlotState.GROWING && p.readyAtEpochMs != null && now >= p.readyAtEpochMs) {
                p.copy(state = PlotState.READY)
            } else p
        }
    }

    /**
     * 種植：消耗成長點（非種子）。田位須已由累計結算日解鎖。
     * 成長點只由每日結算發放；此處只扣點。
     */
    fun plant(
        plots: List<Plot>,
        progress: PlayerProgress,
        plotIndex: Int,
        crop: CropKind,
        now: Long
    ): PlantResult {
        if (progress.clockPaused) {
            return PlantResult(plots, progress, false, "時鐘倒退中，作物獎勵／種植暫停。")
        }
        if (!FarmStageLogic.isPlotUnlocked(progress.totalSettleDays, plotIndex)) {
            val caps = FarmStageLogic.capabilities(progress.totalSettleDays)
            return PlantResult(
                plots, progress, false,
                "此田尚未開墾（${caps.stage.nameZh}僅開放 ${caps.unlockedPlotCount} 格）。繼續每日結算可解鎖。"
            )
        }
        val cost = GrowthSpendCosts.plant(crop)
        if (progress.growthPoints < cost) {
            return PlantResult(plots, progress, false, "成長點不足（需要 $cost）。請先完成每日結算。")
        }
        val plot = plots.getOrNull(plotIndex)
            ?: return PlantResult(plots, progress, false, "無效田地。")
        if (plot.state != PlotState.EMPTY) {
            return PlantResult(plots, progress, false, "此田已有作物。")
        }
        val ready = now + CropTimers.growMs(crop)
        val updatedPlots = plots.map {
            if (it.index == plotIndex) Plot(
                index = plotIndex,
                state = PlotState.GROWING,
                crop = crop,
                plantedAtEpochMs = now,
                readyAtEpochMs = ready
            ) else it
        }
        val updatedProgress = progress.copy(growthPoints = progress.growthPoints - cost)
        return PlantResult(updatedPlots, updatedProgress, true, "已種植${cropZh(crop)}！成長點 -$cost")
    }

    /**
     * 收成：只給種子，不發成長點（成長點只由每日結算發放）。
     */
    fun harvest(
        plots: List<Plot>,
        progress: PlayerProgress,
        plotIndex: Int,
        now: Long
    ): HarvestResult {
        if (progress.clockPaused) {
            return HarvestResult(plots, progress, false, "時鐘倒退中，收成暫停。")
        }
        val plot = plots.getOrNull(plotIndex)
            ?: return HarvestResult(plots, progress, false, "無效田地。")
        val refreshed = refreshPlots(listOf(plot), now, false).first()
        if (refreshed.state != PlotState.READY || refreshed.crop == null) {
            return HarvestResult(plots, progress, false, "尚未可收成。")
        }
        val bonus = CropTimers.harvestSeeds(refreshed.crop)
        val updatedPlots = plots.map {
            if (it.index == plotIndex) Plot(index = plotIndex) else it
        }
        val updatedProgress = progress.copy(seeds = progress.seeds + bonus)
        return HarvestResult(updatedPlots, updatedProgress, true, "收成！種子 +$bonus")
    }

    /** 餵食：消耗成長點；須有寵物欄位（萌芽起）。 */
    fun feedPet(
        pet: PetState,
        progress: PlayerProgress,
        now: Long
    ): SpendResult {
        if (progress.clockPaused) {
            return SpendResult(progress, pet, false, "時鐘倒退中，寵物互動暫停。")
        }
        val caps = FarmStageLogic.capabilities(progress.totalSettleDays)
        if (caps.petFeedSlots < 1) {
            return SpendResult(progress, pet, false, "萌芽階段起才可餵食（累計結算 ${StageRules.SPROUT_DAYS} 日）。")
        }
        val cost = GrowthSpendCosts.FEED_PET
        if (progress.growthPoints < cost) {
            return SpendResult(progress, pet, false, "成長點不足（需要 $cost）。")
        }
        val hunger = (pet.hunger - 20).coerceAtLeast(0)
        val affection = (pet.affection + 5).coerceAtMost(100)
        val updatedPet = pet.copy(hunger = hunger, affection = affection, lastFedEpochMs = now)
        val updatedProgress = progress.copy(growthPoints = progress.growthPoints - cost)
        return SpendResult(updatedProgress, updatedPet, true, "餵食成功！成長點 -$cost")
    }

    fun interactPet(pet: PetState, now: Long, clockPaused: Boolean): Pair<PetState, String> {
        if (clockPaused) return pet to "時鐘倒退中，寵物互動暫停。"
        val affection = (pet.affection + 8).coerceAtMost(100)
        return pet.copy(affection = affection, lastInteractEpochMs = now) to "互動成功！"
    }

    /** 佈置：消耗成長點；安家起有 1 個裝飾欄。 */
    fun buildDecor(
        progress: PlayerProgress,
        placedDecorCount: Int
    ): SpendResult {
        if (progress.clockPaused) {
            return SpendResult(progress, null, false, "時鐘倒退中，建造暫停。")
        }
        val caps = FarmStageLogic.capabilities(progress.totalSettleDays)
        if (caps.decorSlots < 1) {
            return SpendResult(progress, null, false, "安家階段起才可佈置（累計結算 ${StageRules.HOMESTEAD_DAYS} 日）。")
        }
        if (placedDecorCount >= caps.decorSlots) {
            return SpendResult(progress, null, false, "裝飾欄已滿（目前 ${caps.decorSlots} 格）。")
        }
        val cost = GrowthSpendCosts.BUILD_DECOR
        if (progress.growthPoints < cost) {
            return SpendResult(progress, null, false, "成長點不足（需要 $cost）。")
        }
        val updatedProgress = progress.copy(growthPoints = progress.growthPoints - cost)
        return SpendResult(updatedProgress, null, true, "佈置成功！成長點 -$cost")
    }

    fun cropZh(kind: CropKind): String = when (kind) {
        CropKind.WHEAT -> "小麥"
        CropKind.CARROT -> "紅蘿蔔"
        CropKind.TOMATO -> "番茄"
    }
}

