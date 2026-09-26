package com.farmledger.app.domain.model

import kotlinx.serialization.Serializable

enum class EntryType { INCOME, EXPENSE, NO_TRADE, TRANSFER }

enum class EntryStatus { ACTIVE, VOIDED }

/** 真港幣分類（≠種子幣）；對應牧場鏡像物件 */
enum class LedgerCategory(val nameZh: String, val farmObjectZh: String) {
    FOOD("飲食", "餐桌／灶"),
    TRANSPORT("交通", "小路／單車架"),
    HOUSING("住屋", "門廊"),
    DAILY("日用", "木箱"),
    ENTERTAINMENT("娛樂", "花圃／魚塘"),
    HEALTH("健康", "藥草圃"),
    INCOME("收入", "郵箱／收成籃"),
    SAVINGS("儲蓄", "撲滿"),
    OTHER("其他", "告示牌");

    companion object {
        fun fromStorage(raw: String?): LedgerCategory? =
            if (raw.isNullOrBlank()) null
            else entries.find { it.name == raw || it.nameZh == raw }

        /** 常用支出 chips（入帳極速流） */
        val expenseChips: List<LedgerCategory> =
            listOf(FOOD, TRANSPORT, HOUSING, DAILY, ENTERTAINMENT, HEALTH, OTHER)
    }
}

/** 真港幣帳戶（預設「現金」；可免費新增多個） */
@Serializable
data class LedgerAccount(
    val id: String,
    val nameZh: String,
    val archived: Boolean = false
)

object DefaultAccounts {
    const val CASH_ID = "cash"
    const val SAVINGS_ID = "savings"
    val CASH = LedgerAccount(id = CASH_ID, nameZh = "現金")
    val SAVINGS = LedgerAccount(id = SAVINGS_ID, nameZh = "儲蓄")
}

@Serializable
data class LedgerEntry(
    val id: String,
    val localDate: String,           // yyyy-MM-dd
    val type: EntryType,
    val amountMinor: Long,           // 港幣分；NO_TRADE 為 0；≠種子幣
    val note: String = "",
    val category: String? = null,    // LedgerCategory.name；可空
    val accountId: String = DefaultAccounts.CASH_ID,
    /** TRANSFER 時對方帳戶；其他類型為 null */
    val transferAccountId: String? = null,
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
    /** 牧場種子幣（≠港幣）；商店買賣只動此欄＋背包 */
    val seedCoins: Int = 20,
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
    val unlockedStreakRewards: Set<Int> = emptySet(), // 3,5,7（種子加成，與場景階段分開）
    /**
     * 已用種子幣建造／購買嘅熱區 id（home/food/…）。
     * 場上可見＝shop_day_reached AND id∈此集合；開局 empty＝淨農地。
     */
    val ownedHotspotIds: Set<String> = emptySet()
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
    /** 有記／記全日結固定種子幣（唔跟金額／筆數） */
    const val DAILY_SEED_COINS = 1
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

/** 場景內消耗成長點（成長點只由每日結算發放；種植耗種子、餵食耗飼料） */
object GrowthSpendCosts {
    /** @deprecated M3 起餵食改耗背包飼料，不再扣成長點 */
    const val FEED_PET = 1
    const val BUILD_DECOR = 1
}

/** M3：餵食消耗背包飼料（唔扣成長點、唔發成長點） */
object InventoryFeedCosts {
    const val FEED_PER_MEAL = 1
}

/**
 * 牧場內經濟標價（種子幣；≠港幣）。
 * 買賣只動種子幣／背包，**唔寫**真港幣 ledger_entries，唔發成長點。
 */
object ShopCatalog {
    fun seedBuyPriceCoins(kind: CropKind): Int = when (kind) {
        CropKind.WHEAT -> 2
        CropKind.CARROT -> 3
        CropKind.TOMATO -> 4
    }

    fun cropSellPriceCoins(kind: CropKind): Int = when (kind) {
        CropKind.WHEAT -> 3
        CropKind.CARROT -> 5
        CropKind.TOMATO -> 7
    }

    /** 買飼料單價（種子幣） */
    const val FEED_BUY_PRICE_COINS = 2

    @Deprecated("M5：商店唔再用港幣分；請用 seedBuyPriceCoins")
    fun seedBuyPriceMinor(kind: CropKind): Long = seedBuyPriceCoins(kind).toLong()

    @Deprecated("M5：商店唔再用港幣分；請用 cropSellPriceCoins")
    fun cropSellPriceMinor(kind: CropKind): Long = cropSellPriceCoins(kind).toLong()

    @Deprecated("M5：請用 FEED_BUY_PRICE_COINS")
    const val FEED_BUY_PRICE_MINOR = 2L
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
    FEED("飼料"),
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
