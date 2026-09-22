package com.farmledger.app.domain.usecase

import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.InventoryItemKind
import com.farmledger.app.domain.model.CropTimers
import com.farmledger.app.domain.model.FarmStage
import com.farmledger.app.domain.model.PetState
import com.farmledger.app.domain.model.PlayerProgress
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
    fun plantSpendsInventorySeeds_notAwardedOnHarvest() {
        val progress = PlayerProgress(growthPoints = 5, totalSettleDays = 3)
        val plots = FarmLogic.defaultPlots()
        val inv0 = InventoryLogic.add(
            InventoryLogic.emptyStubs(0L),
            InventoryItemKind.SEED_WHEAT,
            3,
            1L
        ).inventory
        val planted = FarmLogic.plant(plots, progress, inv0, 0, CropKind.WHEAT, now = 1_000L)
        assertThat(planted.ok).isTrue()
        // 種植耗背包種子，唔扣成長點
        assertThat(planted.progress.growthPoints).isEqualTo(5)
        assertThat(InventoryLogic.seedQty(planted.inventory, CropKind.WHEAT))
            .isEqualTo(3 - CropTimers.seedCost(CropKind.WHEAT))

        val growing = planted.plots.map {
            if (it.index == 0) it.copy(
                state = PlotState.READY,
                crop = CropKind.WHEAT,
                watered = true
            ) else it
        }
        val harvested = FarmLogic.harvest(growing, planted.progress, planted.inventory, 0, now = 2_000L)
        assertThat(harvested.ok).isTrue()
        // 收成不發成長點；作物入背包
        assertThat(harvested.progress.growthPoints).isEqualTo(planted.progress.growthPoints)
        assertThat(InventoryLogic.cropQty(harvested.inventory, CropKind.WHEAT))
            .isGreaterThan(InventoryLogic.cropQty(planted.inventory, CropKind.WHEAT))
    }

    @Test
    fun plantBlocked_onLockedPlot() {
        val progress = PlayerProgress(growthPoints = 10, totalSettleDays = 0) // 荒地
        val inv = InventoryLogic.add(
            InventoryLogic.emptyStubs(0L),
            InventoryItemKind.SEED_WHEAT,
            5,
            1L
        ).inventory
        val r = FarmLogic.plant(FarmLogic.defaultPlots(), progress, inv, 3, CropKind.WHEAT, 1L)
        assertThat(r.ok).isFalse()
        assertThat(r.msg).contains("開墾")
    }

    @Test
    fun feedRequiresSproutAndConsumesInventoryFeed_notGrowthPoints() {
        val inv = InventoryLogic.add(
            InventoryLogic.emptyStubs(0L), InventoryItemKind.FEED, 2, 1L
        ).inventory
        val barren = PlayerProgress(growthPoints = 5, totalSettleDays = 0)
        val blocked = FarmLogic.feedPet(PetState(), barren, inv, 1L)
        assertThat(blocked.ok).isFalse()

        val sprout = PlayerProgress(growthPoints = 5, totalSettleDays = 3)
        val gpBefore = sprout.growthPoints
        val fed = FarmLogic.feedPet(PetState(), sprout, inv, 1L)
        assertThat(fed.ok).isTrue()
        assertThat(fed.progress.growthPoints).isEqualTo(gpBefore)
        assertThat(InventoryLogic.feedQty(fed.inventory)).isEqualTo(1)
        assertThat(fed.msg).contains("餵食成功")
    }
}

