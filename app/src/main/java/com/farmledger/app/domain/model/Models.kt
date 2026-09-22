package com.farmledger.app.domain.model

import kotlinx.serialization.Serializable

enum class EntryType { INCOME, EXPENSE, NO_TRADE }

enum class EntryStatus { ACTIVE, VOIDED }

@Serializable
data class LedgerEntry(
    val id: String,
    val localDate: String,           // yyyy-MM-dd
    val type: EntryType,
    val amountMinor: Long,           // 分/角最小單位；NO_TRADE 為 0
    val note: String = "",
    val status: EntryStatus = EntryStatus.ACTIVE,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Serializable
data class DailySettlement(
    val localDate: String,
    val growthPointsAwarded: Int,    // 固定 1，一日一次
    val settledAtEpochMs: Long
)

enum class CropKind { WHEAT, CARROT, TOMATO }

enum class PlotState { EMPTY, GROWING, READY }

@Serializable
data class Plot(
    val index: Int,                  // 0..5
    val state: PlotState = PlotState.EMPTY,
    val crop: CropKind? = null,
    val plantedAtEpochMs: Long? = null,
    val readyAtEpochMs: Long? = null,
    /** M2：澆水後才開始成長計時；未澆水唔會變 READY */
    val watered: Boolean = false
)

@Serializable
data class PetState(
    val name: String = "小農寵",
    val hunger: Int = 50,            // 0..100
    val affection: Int = 50,         // 0..100
    val lastFedEpochMs: Long = 0,
    val lastInteractEpochMs: Long = 0
)

@Serializable
data class Decoration(
    val id: String,
    val nameZh: String,
    val unlocked: Boolean = false,
    val placed: Boolean = false
)

@Serializable
data class PlayerProgress(
    val growthPoints: Int = 0,
    /** 舊欄位：M2 起種子以 inventory 為準；保留作遷移／相容 */
    val seeds: Int = 3,
    val streakDays: Int = 0,
    /** 累計成功結算日數（每本地日最多 +1；用於階段解鎖，非記帳筆數） */
    val totalSettleDays: Int = 0,
    val lastSettleDate: String? = null,
    val lastKnownLocalDate: String? = null,
    val clockPaused: Boolean = false,   // 時鐘倒退時暫停獎勵
    val onboardingDone: Boolean = false,
    val weeklyReviewClaimedWeekId: String? = null,
    val unlockedStreakRewards: Set<Int> = emptySet() // 3,5,7（種子加成，與場景階段分開）
)

/** 農場場景階段（由累計結算日解鎖） */
enum class FarmStage(val nameZh: String) {
    BARREN("荒地"),
    SPROUT("萌芽"),
    HOMESTEAD("安家"),
    THRIVING("旺場")
}

/** 作物成長所需毫秒（示範用短時間，方便 MVP 體驗） */
object CropTimers {
    fun growMs(kind: CropKind): Long = when (kind) {
        CropKind.WHEAT -> 60_000L      // 1 分鐘
        CropKind.CARROT -> 120_000L    // 2 分鐘
        CropKind.TOMATO -> 180_000L    // 3 分鐘
    }

    /** 種植消耗背包種子數 */
    fun seedCost(kind: CropKind): Int = when (kind) {
        CropKind.WHEAT -> 1
        CropKind.CARROT -> 1
        CropKind.TOMATO -> 2
    }

    /** 收成進背包的作物堆疊數（不發成長點） */
    fun harvestYield(kind: CropKind): Int = when (kind) {
        CropKind.WHEAT -> 2
        CropKind.CARROT -> 2
        CropKind.TOMATO -> 3
    }

    @Deprecated("改用 harvestYield；舊名保留相容")
    fun harvestSeeds(kind: CropKind): Int = harvestYield(kind)
}

object RewardRules {
    const val DAILY_GROWTH_POINTS = 1
    const val WEEKLY_REVIEW_CAP_POINTS = 3
    val STREAK_MILESTONES = listOf(3, 5, 7)
}

/** 場景階段門檻：累計結算日（非連續、非記帳筆數） */
object StageRules {
    const val SPROUT_DAYS = 3
    const val HOMESTEAD_DAYS = 7
    const val THRIVING_DAYS = 14
    const val THRIVING_ANIMAL2_DAYS = 21
    val UNLOCK_DAYS = listOf(SPROUT_DAYS, HOMESTEAD_DAYS, THRIVING_DAYS, THRIVING_ANIMAL2_DAYS)
}

/** 場景內消耗成長點（成長點只由每日結算發放；種植改耗背包種子） */
object GrowthSpendCosts {
    const val FEED_PET = 1
    const val BUILD_DECOR = 1
}

/** 商店標價（amountMinor；買賣只寫帳、不發成長點） */
object ShopCatalog {
    fun seedBuyPriceMinor(kind: CropKind): Long = when (kind) {
        CropKind.WHEAT -> 100L
        CropKind.CARROT -> 150L
        CropKind.TOMATO -> 200L
    }

    fun cropSellPriceMinor(kind: CropKind): Long = when (kind) {
        CropKind.WHEAT -> 200L
        CropKind.CARROT -> 300L
        CropKind.TOMATO -> 450L
    }
}

/** 日內時段（牧場物語式日循環） */
enum class DayPhase(val nameZh: String) {
    MORNING("晨"),
    NOON("晝"),
    EVENING("昏"),
    NIGHT("夜");

    fun nextOrNull(): DayPhase? = when (this) {
        MORNING -> NOON
        NOON -> EVENING
        EVENING -> NIGHT
        NIGHT -> null
    }

    companion object {
        fun fromName(name: String): DayPhase =
            entries.find { it.name == name } ?: MORNING
    }
}

/**
 * 遊戲日／時段狀態（離線 Room 持久化）。
 * 與結算發獎正交：睡覺只推進日與時段，不發成長點。
 */
@Serializable
data class GameDayState(
    val gameDay: Int = 1,
    val phase: DayPhase = DayPhase.MORNING,
    /** 快取農場階段名（由累計結算日推導，存檔方便 HUD／場景） */
    val farmStageHint: String = FarmStage.BARREN.name,
    val lastWallClockEpochMs: Long = 0L,
    val updatedAtEpochMs: Long = 0L
)

/** M2：背包物品——種子／收成堆疊 */
enum class InventoryItemKind(val nameZh: String) {
    SEED_WHEAT("小麥種子"),
    SEED_CARROT("紅蘿蔔種子"),
    SEED_TOMATO("番茄種子"),
    CROP_WHEAT("小麥"),
    CROP_CARROT("紅蘿蔔"),
    CROP_TOMATO("番茄"),
    MATERIAL("材料");

    companion object {
        fun seedOf(crop: CropKind): InventoryItemKind = when (crop) {
            CropKind.WHEAT -> SEED_WHEAT
            CropKind.CARROT -> SEED_CARROT
            CropKind.TOMATO -> SEED_TOMATO
        }

        fun cropOf(crop: CropKind): InventoryItemKind = when (crop) {
            CropKind.WHEAT -> CROP_WHEAT
            CropKind.CARROT -> CROP_CARROT
            CropKind.TOMATO -> CROP_TOMATO
        }

        fun fromStorage(name: String): InventoryItemKind = when (name) {
            "SEED_BAG" -> SEED_WHEAT // M1 stub 遷移
            else -> entries.find { it.name == name } ?: MATERIAL
        }
    }
}

@Serializable
data class InventoryItem(
    val id: String,
    val kind: InventoryItemKind,
    val quantity: Int = 0,
    val updatedAtEpochMs: Long = 0L
)
