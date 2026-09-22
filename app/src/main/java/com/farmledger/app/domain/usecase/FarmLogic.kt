package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.CropTimers
import com.farmledger.app.domain.model.GrowthSpendCosts
import com.farmledger.app.domain.model.InventoryFeedCosts
import com.farmledger.app.domain.model.InventoryItem
import com.farmledger.app.domain.model.InventoryItemKind
import com.farmledger.app.domain.model.StageRules
import com.farmledger.app.domain.model.PetState
import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.Plot
import com.farmledger.app.domain.model.PlotState

data class PlantResult(
    val plots: List<Plot>,
    val inventory: List<InventoryItem>,
    val progress: PlayerProgress,
    val ok: Boolean,
    val msg: String
)

data class WaterResult(
    val plots: List<Plot>,
    val ok: Boolean,
    val msg: String
)

data class HarvestResult(
    val plots: List<Plot>,
    val inventory: List<InventoryItem>,
    val progress: PlayerProgress,
    val ok: Boolean,
    val msg: String
)

data class SpendResult(val progress: PlayerProgress, val pet: PetState? = null, val ok: Boolean, val msg: String)

/** M3：餵食結果——扣背包飼料，唔扣成長點 */
data class FeedResult(
    val progress: PlayerProgress,
    val pet: PetState?,
    val inventory: List<InventoryItem>,
    val ok: Boolean,
    val msg: String
)

object FarmLogic {

    fun defaultPlots(): List<Plot> = (0 until 6).map { Plot(index = it) }

    /**
     * 刷新田格：僅已澆水的 GROWING 可於 readyAt 轉 READY。
     * 時鐘暫停時不推進。
     */
    fun refreshPlots(plots: List<Plot>, now: Long, clockPaused: Boolean): List<Plot> {
        if (clockPaused) return plots
        return plots.map { p ->
            if (
                p.state == PlotState.GROWING &&
                p.watered &&
                p.readyAtEpochMs != null &&
                now >= p.readyAtEpochMs
            ) {
                p.copy(state = PlotState.READY)
            } else p
        }
    }

    /**
     * 種植：消耗背包種子（非成長點）。田位須已解鎖。
     * 種下後須澆水才開始計時。
     */
    fun plant(
        plots: List<Plot>,
        progress: PlayerProgress,
        inventory: List<InventoryItem>,
        plotIndex: Int,
        crop: CropKind,
        now: Long
    ): PlantResult {
        if (progress.clockPaused) {
            return PlantResult(plots, inventory, progress, false, "時鐘倒退中，種植暫停。")
        }
        if (!FarmStageLogic.isPlotUnlocked(progress.totalSettleDays, plotIndex)) {
            val caps = FarmStageLogic.capabilities(progress.totalSettleDays)
            return PlantResult(
                plots, inventory, progress, false,
                "此田尚未開墾（${caps.stage.nameZh}僅開放 ${caps.unlockedPlotCount} 格）。繼續每日結算可解鎖。"
            )
        }
        val plot = plots.getOrNull(plotIndex)
            ?: return PlantResult(plots, inventory, progress, false, "無效田地。")
        if (plot.state != PlotState.EMPTY) {
            return PlantResult(plots, inventory, progress, false, "此田已有作物。")
        }
        val cost = CropTimers.seedCost(crop)
        val seedKind = InventoryItemKind.seedOf(crop)
        val removed = InventoryLogic.remove(inventory, seedKind, cost, now)
        if (!removed.ok) {
            return PlantResult(plots, inventory, progress, false, "種子不足：${removed.msg} 可到商店購買。")
        }
        val updatedPlots = plots.map {
            if (it.index == plotIndex) Plot(
                index = plotIndex,
                state = PlotState.GROWING,
                crop = crop,
                plantedAtEpochMs = now,
                readyAtEpochMs = null,
                watered = false
            ) else it
        }
        return PlantResult(
            updatedPlots,
            removed.inventory,
            progress,
            true,
            "已種植${cropZh(crop)}（種子 -$cost）。請澆水開始成長！"
        )
    }

    /**
     * 澆水：GROWING 且未澆水 → 開始成長計時。不發成長點、不改背包。
     */
    fun water(
        plots: List<Plot>,
        progress: PlayerProgress,
        plotIndex: Int,
        now: Long
    ): WaterResult {
        if (progress.clockPaused) {
            return WaterResult(plots, false, "時鐘倒退中，澆水暫停。")
        }
        val plot = plots.getOrNull(plotIndex)
            ?: return WaterResult(plots, false, "無效田地。")
        if (plot.state != PlotState.GROWING || plot.crop == null) {
            return WaterResult(plots, false, "此田無需澆水。")
        }
        if (plot.watered) {
            return WaterResult(plots, false, "已經澆過水。")
        }
        val ready = now + CropTimers.growMs(plot.crop)
        val updated = plots.map {
            if (it.index == plotIndex) it.copy(watered = true, readyAtEpochMs = ready) else it
        }
        return WaterResult(updated, true, "已澆水！${cropZh(plot.crop)}開始成長。")
    }

    /**
     * 收成：作物進背包堆疊，不發成長點。
     */
    fun harvest(
        plots: List<Plot>,
        progress: PlayerProgress,
        inventory: List<InventoryItem>,
        plotIndex: Int,
        now: Long
    ): HarvestResult {
        if (progress.clockPaused) {
            return HarvestResult(plots, inventory, progress, false, "時鐘倒退中，收成暫停。")
        }
        val plot = plots.getOrNull(plotIndex)
            ?: return HarvestResult(plots, inventory, progress, false, "無效田地。")
        val refreshed = refreshPlots(listOf(plot), now, false).first()
        if (refreshed.state != PlotState.READY || refreshed.crop == null) {
            return HarvestResult(plots, inventory, progress, false, "尚未可收成。")
        }
        val yield = CropTimers.harvestYield(refreshed.crop)
        val cropKind = InventoryItemKind.cropOf(refreshed.crop)
        val added = InventoryLogic.add(inventory, cropKind, yield, now)
        val updatedPlots = plots.map {
            if (it.index == plotIndex) Plot(index = plotIndex) else it
        }
        return HarvestResult(
            updatedPlots,
            added.inventory,
            progress,
            true,
            "收成！${cropZh(refreshed.crop)} +$yield（已入背包，唔發成長點）"
        )
    }

    /** 餵食：消耗背包飼料；須有寵物欄位（萌芽起）。唔扣／唔發成長點。 */
    fun feedPet(
        pet: PetState,
        progress: PlayerProgress,
        inventory: List<InventoryItem>,
        now: Long
    ): FeedResult {
        if (progress.clockPaused) {
            return FeedResult(progress, pet, inventory, false, "時鐘倒退中，寵物互動暫停。")
        }
        val caps = FarmStageLogic.capabilities(progress.totalSettleDays)
        if (caps.petFeedSlots < 1) {
            return FeedResult(
                progress, pet, inventory, false,
                "萌芽階段起才可餵食（累計結算 ${StageRules.SPROUT_DAYS} 日）。"
            )
        }
        val cost = InventoryFeedCosts.FEED_PER_MEAL
        val removed = InventoryLogic.remove(inventory, InventoryItemKind.FEED, cost, now)
        if (!removed.ok) {
            return FeedResult(progress, pet, inventory, false, "飼料不足，請去商店買飼料。")
        }
        val hunger = (pet.hunger - 20).coerceAtLeast(0)
        val affection = (pet.affection + 5).coerceAtMost(100)
        val updatedPet = pet.copy(hunger = hunger, affection = affection, lastFedEpochMs = now)
        return FeedResult(
            progress = progress,
            pet = updatedPet,
            inventory = removed.inventory,
            ok = true,
            msg = "餵食成功！寵物開心拍食（飼料 -$cost）"
        )
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
