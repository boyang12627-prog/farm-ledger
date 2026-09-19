package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.CropTimers
import com.farmledger.app.domain.model.PetState
import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.Plot
import com.farmledger.app.domain.model.PlotState

data class PlantResult(val plots: List<Plot>, val progress: PlayerProgress, val ok: Boolean, val msg: String)
data class HarvestResult(val plots: List<Plot>, val progress: PlayerProgress, val ok: Boolean, val msg: String)

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
        val cost = CropTimers.seedCost(crop)
        if (progress.seeds < cost) {
            return PlantResult(plots, progress, false, "種子不足（需要 $cost）。")
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
        val updatedProgress = progress.copy(seeds = progress.seeds - cost)
        return PlantResult(updatedPlots, updatedProgress, true, "已種植${cropZh(crop)}！")
    }

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
        val updatedProgress = progress.copy(
            seeds = progress.seeds + bonus,
            growthPoints = progress.growthPoints + 1
        )
        return HarvestResult(updatedPlots, updatedProgress, true, "收成！種子 +$bonus、成長點 +1")
    }

    fun feedPet(pet: PetState, now: Long, clockPaused: Boolean): Pair<PetState, String> {
        if (clockPaused) return pet to "時鐘倒退中，寵物互動暫停。"
        val hunger = (pet.hunger - 20).coerceAtLeast(0)
        val affection = (pet.affection + 5).coerceAtMost(100)
        return pet.copy(hunger = hunger, affection = affection, lastFedEpochMs = now) to "餵食成功！"
    }

    fun interactPet(pet: PetState, now: Long, clockPaused: Boolean): Pair<PetState, String> {
        if (clockPaused) return pet to "時鐘倒退中，寵物互動暫停。"
        val affection = (pet.affection + 8).coerceAtMost(100)
        return pet.copy(affection = affection, lastInteractEpochMs = now) to "互動成功！"
    }

    fun cropZh(kind: CropKind): String = when (kind) {
        CropKind.WHEAT -> "小麥"
        CropKind.CARROT -> "紅蘿蔔"
        CropKind.TOMATO -> "番茄"
    }
}
