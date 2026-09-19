package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.FarmStage
import com.farmledger.app.domain.model.GrowthSpendCosts
import com.farmledger.app.domain.model.PetState
import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.Plot
import com.farmledger.app.domain.model.PlotState
import com.farmledger.app.domain.model.StageRules
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FarmStageLogicTest {

    @Test
    fun stageUnlocks_byCumulativeSettleDays_notEntrySpam() {
        assertThat(FarmStageLogic.stageFor(0)).isEqualTo(FarmStage.BARREN)
        assertThat(FarmStageLogic.stageFor(2)).isEqualTo(FarmStage.BARREN)
        assertThat(FarmStageLogic.stageFor(StageRules.SPROUT_DAYS)).isEqualTo(FarmStage.SPROUT)
        assertThat(FarmStageLogic.stageFor(StageRules.HOMESTEAD_DAYS)).isEqualTo(FarmStage.HOMESTEAD)
        assertThat(FarmStageLogic.stageFor(StageRules.THRIVING_DAYS)).isEqualTo(FarmStage.THRIVING)
        assertThat(FarmStageLogic.stageFor(StageRules.THRIVING_ANIMAL2_DAYS)).isEqualTo(FarmStage.THRIVING)
    }

    @Test
    fun unlockThresholds_are_3_7_14_21() {
        assertThat(StageRules.UNLOCK_DAYS).containsExactly(3, 7, 14, 21).inOrder()
    }

    @Test
    fun barren_onlyTwoPlots() {
        val caps = FarmStageLogic.capabilities(0)
        assertThat(caps.unlockedPlotCount).isEqualTo(2)
        assertThat(caps.showGrassEdges).isFalse()
        assertThat(caps.showHut).isFalse()
        assertThat(FarmStageLogic.isPlotUnlocked(0, 0)).isTrue()
        assertThat(FarmStageLogic.isPlotUnlocked(0, 1)).isTrue()
        assertThat(FarmStageLogic.isPlotUnlocked(0, 2)).isFalse()
    }

    @Test
    fun sprout_unlocksSixPlotsAndPetFeed() {
        val caps = FarmStageLogic.capabilities(3)
        assertThat(caps.stage.nameZh).isEqualTo("萌芽")
        assertThat(caps.unlockedPlotCount).isEqualTo(6)
        assertThat(caps.petFeedSlots).isEqualTo(1)
        assertThat(caps.showGrassEdges).isTrue()
    }

    @Test
    fun homestead_unlocksHutFenceDecor() {
        val caps = FarmStageLogic.capabilities(7)
        assertThat(caps.stage.nameZh).isEqualTo("安家")
        assertThat(caps.showHut).isTrue()
        assertThat(caps.showFence).isTrue()
        assertThat(caps.decorSlots).isEqualTo(1)
    }

    @Test
    fun thriving_greenerAndSecondAnimalAt21() {
        val at14 = FarmStageLogic.capabilities(14)
        assertThat(at14.stage.nameZh).isEqualTo("旺場")
        assertThat(at14.greenerDenser).isTrue()
        assertThat(at14.smallExpansion).isTrue()
        assertThat(at14.animalSlots).isEqualTo(1)

        val at21 = FarmStageLogic.capabilities(21)
        assertThat(at21.animalSlots).isEqualTo(2)
    }

    @Test
    fun settleIncrementsTotalSettleDays_forStageProgress() {
        val base = PlayerProgress(totalSettleDays = 2, lastSettleDate = "2026-09-17")
        val r = DailySettlementLogic.settle(base, "2026-09-18", 1L, hasAnyLedgerActivity = true)
        assertThat(r.awarded).isTrue()
        assertThat(r.progress.totalSettleDays).isEqualTo(3)
        assertThat(FarmStageLogic.stageFor(r.progress.totalSettleDays)).isEqualTo(FarmStage.SPROUT)
    }

    @Test
    fun plantSpendsGrowthPoints_notAwardedOnHarvest() {
        val progress = PlayerProgress(growthPoints = 5, totalSettleDays = 3)
        val plots = FarmLogic.defaultPlots()
        val planted = FarmLogic.plant(plots, progress, 0, CropKind.WHEAT, now = 1_000L)
        assertThat(planted.ok).isTrue()
        assertThat(planted.progress.growthPoints).isEqualTo(5 - GrowthSpendCosts.plant(CropKind.WHEAT))

        val growing = planted.plots.map {
            if (it.index == 0) it.copy(state = PlotState.READY, crop = CropKind.WHEAT) else it
        }
        val harvested = FarmLogic.harvest(growing, planted.progress, 0, now = 2_000L)
        assertThat(harvested.ok).isTrue()
        // 收成不發成長點
        assertThat(harvested.progress.growthPoints).isEqualTo(planted.progress.growthPoints)
        assertThat(harvested.progress.seeds).isGreaterThan(planted.progress.seeds)
    }

    @Test
    fun plantBlocked_onLockedPlot() {
        val progress = PlayerProgress(growthPoints = 10, totalSettleDays = 0) // 荒地
        val r = FarmLogic.plant(FarmLogic.defaultPlots(), progress, 3, CropKind.WHEAT, 1L)
        assertThat(r.ok).isFalse()
        assertThat(r.msg).contains("開墾")
    }

    @Test
    fun feedRequiresSproutAndSpendsGrowthPoints() {
        val barren = PlayerProgress(growthPoints = 5, totalSettleDays = 0)
        val blocked = FarmLogic.feedPet(PetState(), barren, 1L)
        assertThat(blocked.ok).isFalse()

        val sprout = PlayerProgress(growthPoints = 5, totalSettleDays = 3)
        val fed = FarmLogic.feedPet(PetState(), sprout, 1L)
        assertThat(fed.ok).isTrue()
        assertThat(fed.progress.growthPoints).isEqualTo(4)
    }
}
