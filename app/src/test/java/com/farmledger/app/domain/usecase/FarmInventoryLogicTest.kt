package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.CropTimers
import com.farmledger.app.domain.model.InventoryItemKind
import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.PlotState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FarmInventoryLogicTest {

    private val progress = PlayerProgress(
        growthPoints = 10,
        seeds = 0,
        totalSettleDays = 3, // sprout：6 格
        clockPaused = false
    )

    private fun invWithSeeds(wheat: Int = 5) =
        InventoryLogic.add(InventoryLogic.emptyStubs(0L), InventoryItemKind.SEED_WHEAT, wheat, 1L).inventory

    @Test
    fun plant_consumesInventorySeeds_notGrowthPoints() {
        val inv = invWithSeeds(3)
        val gpBefore = progress.growthPoints
        val r = FarmLogic.plant(
            plots = FarmLogic.defaultPlots(),
            progress = progress,
            inventory = inv,
            plotIndex = 0,
            crop = CropKind.WHEAT,
            now = 1_000L
        )
        assertThat(r.ok).isTrue()
        assertThat(r.progress.growthPoints).isEqualTo(gpBefore)
        assertThat(InventoryLogic.seedQty(r.inventory, CropKind.WHEAT))
            .isEqualTo(3 - CropTimers.seedCost(CropKind.WHEAT))
        assertThat(r.plots[0].state).isEqualTo(PlotState.GROWING)
        assertThat(r.plots[0].watered).isFalse()
        assertThat(r.plots[0].readyAtEpochMs).isNull()
    }

    @Test
    fun water_startsGrowthTimer_unwateredCannotReady() {
        val planted = FarmLogic.plant(
            FarmLogic.defaultPlots(), progress, invWithSeeds(), 0, CropKind.WHEAT, 1_000L
        )
        assertThat(planted.ok).isTrue()

        val early = FarmLogic.refreshPlots(planted.plots, now = 1_000L + CropTimers.growMs(CropKind.WHEAT) + 1, false)
        assertThat(early[0].state).isEqualTo(PlotState.GROWING)

        val watered = FarmLogic.water(planted.plots, progress, 0, now = 2_000L)
        assertThat(watered.ok).isTrue()
        assertThat(watered.plots[0].watered).isTrue()
        assertThat(watered.plots[0].readyAtEpochMs).isEqualTo(2_000L + CropTimers.growMs(CropKind.WHEAT))

        val ready = FarmLogic.refreshPlots(
            watered.plots,
            now = 2_000L + CropTimers.growMs(CropKind.WHEAT),
            clockPaused = false
        )
        assertThat(ready[0].state).isEqualTo(PlotState.READY)
    }

    @Test
    fun harvest_addsCropStacks_notGrowthPoints() {
        val planted = FarmLogic.plant(
            FarmLogic.defaultPlots(), progress, invWithSeeds(), 0, CropKind.WHEAT, 1_000L
        )
        val watered = FarmLogic.water(planted.plots, progress, 0, 2_000L)
        val readyAt = watered.plots[0].readyAtEpochMs!!
        val readyPlots = FarmLogic.refreshPlots(watered.plots, readyAt, false)
        val gpBefore = progress.growthPoints
        val r = FarmLogic.harvest(readyPlots, progress, planted.inventory, 0, readyAt)
        assertThat(r.ok).isTrue()
        assertThat(r.progress.growthPoints).isEqualTo(gpBefore)
        assertThat(InventoryLogic.cropQty(r.inventory, CropKind.WHEAT))
            .isEqualTo(CropTimers.harvestYield(CropKind.WHEAT))
        assertThat(r.plots[0].state).isEqualTo(PlotState.EMPTY)
        assertThat(r.msg).contains("唔發成長點")
    }

    @Test
    fun plant_failsWithoutSeeds() {
        val r = FarmLogic.plant(
            FarmLogic.defaultPlots(), progress, InventoryLogic.emptyStubs(), 0, CropKind.WHEAT, 1L
        )
        assertThat(r.ok).isFalse()
        assertThat(r.msg).contains("種子")
    }

    @Test
    fun doubleWater_rejected() {
        val planted = FarmLogic.plant(
            FarmLogic.defaultPlots(), progress, invWithSeeds(), 0, CropKind.WHEAT, 1L
        )
        assertThat(planted.ok).isTrue()
        val w1 = FarmLogic.water(planted.plots, progress, 0, 2L)
        val w2 = FarmLogic.water(w1.plots, progress, 0, 3L)
        assertThat(w1.ok).isTrue()
        assertThat(w2.ok).isFalse()
    }
}
