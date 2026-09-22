package com.farmledger.app.data.repository

import com.farmledger.app.data.local.dao.GameDayDao
import com.farmledger.app.data.local.dao.InventoryDao
import com.farmledger.app.data.local.dao.LedgerDao
import com.farmledger.app.data.local.dao.SettlementDao
import com.farmledger.app.data.local.datastore.GamePreferences
import com.farmledger.app.data.local.entity.DailySettlementEntity
import com.farmledger.app.data.local.entity.GameDayStateEntity
import com.farmledger.app.data.local.entity.InventoryItemEntity
import com.farmledger.app.data.local.entity.LedgerEntryEntity
import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.DailySettlement
import com.farmledger.app.domain.model.DayPhase
import com.farmledger.app.domain.model.Decoration
import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.FarmStage
import com.farmledger.app.domain.model.GameDayState
import com.farmledger.app.domain.model.InventoryItem
import com.farmledger.app.domain.model.InventoryItemKind
import com.farmledger.app.domain.model.LedgerEntry
import com.farmledger.app.domain.model.PetState
import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.Plot
import com.farmledger.app.domain.usecase.DailySettlementLogic
import com.farmledger.app.domain.usecase.DayPhaseLogic
import com.farmledger.app.domain.usecase.DayPhaseResult
import com.farmledger.app.domain.usecase.FarmLogic
import com.farmledger.app.domain.usecase.InventoryLogic
import com.farmledger.app.domain.usecase.FarmStageLogic
import com.farmledger.app.domain.usecase.SettlementResult
import com.farmledger.app.domain.usecase.WeeklyReviewLogic
import com.farmledger.app.domain.usecase.WeeklyReviewResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.UUID

class FarmLedgerRepository(
    private val ledgerDao: LedgerDao,
    private val settlementDao: SettlementDao,
    private val gameDayDao: GameDayDao,
    private val inventoryDao: InventoryDao,
    private val prefs: GamePreferences
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }

    val progressFlow: Flow<PlayerProgress> = prefs.progressFlow
    val plotsFlow: Flow<List<Plot>> = prefs.plotsFlow
    val petFlow: Flow<PetState> = prefs.petFlow
    val decorationsFlow: Flow<List<Decoration>> = prefs.decorationsFlow

    val gameDayFlow: Flow<GameDayState> = gameDayDao.observe().map { entity ->
        entity?.toDomain() ?: GameDayState()
    }

    /** M2 stub：背包觀察（空表為預設） */
    val inventoryFlow: Flow<List<InventoryItem>> = inventoryDao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    fun observeEntries(date: String): Flow<List<LedgerEntry>> =
        ledgerDao.observeByDate(date).map { list -> list.map { it.toDomain() } }

    suspend fun syncClock(today: String = LocalDate.now().toString()) {
        val p = prefs.progressFlow.first()
        var updated = DailySettlementLogic.detectClockRegression(p, today)
        val now = System.currentTimeMillis()
        val day = ensureGameDay(now)
        val stage = FarmStageLogic.stageFor(updated.totalSettleDays)
        if (DayPhaseLogic.isWallClockRegression(day, now)) {
            updated = updated.copy(clockPaused = true)
        } else if (!updated.clockPaused) {
            val touched = DayPhaseLogic.touchWallClock(day, now, stage)
            if (touched != day) gameDayDao.upsert(touched.toEntity())
        }
        if (updated != p) prefs.saveProgress(updated)
    }

    private suspend fun ensureGameDay(nowEpochMs: Long = System.currentTimeMillis()): GameDayState {
        val existing = gameDayDao.get()?.toDomain()
        if (existing != null) return existing
        val fresh = GameDayState(
            gameDay = 1,
            phase = DayPhase.MORNING,
            farmStageHint = FarmStage.BARREN.name,
            lastWallClockEpochMs = nowEpochMs,
            updatedAtEpochMs = nowEpochMs
        )
        gameDayDao.upsert(fresh.toEntity())
        return fresh
    }

    suspend fun waitNextPhase(): DayPhaseResult {
        syncClock()
        val progress = prefs.progressFlow.first()
        val stage = FarmStageLogic.stageFor(progress.totalSettleDays)
        val now = System.currentTimeMillis()
        val current = ensureGameDay(now)
        val result = DayPhaseLogic.waitNextPhase(current, progress.clockPaused, now, stage)
        if (result.advanced) gameDayDao.upsert(result.state.toEntity())
        return result
    }

    suspend fun sleepToNextDay(): DayPhaseResult {
        syncClock()
        val progress = prefs.progressFlow.first()
        val stage = FarmStageLogic.stageFor(progress.totalSettleDays)
        val now = System.currentTimeMillis()
        val current = ensureGameDay(now)
        val today = LocalDate.now().toString()
        val todaySettled = progress.lastSettleDate == today
        val result = DayPhaseLogic.sleepToNextDay(
            current,
            progress.clockPaused,
            now,
            stage,
            todaySettled = todaySettled
        )
        if (result.advanced) gameDayDao.upsert(result.state.toEntity())
        return result
    }

    /**
     * 確保背包列齊全；首次以 progress.seeds 遷移為小麥種子。
     * 買賣／種植／收成皆經背包；數量唔發成長點。
     */
    suspend fun ensureInventoryStubs() {
        val now = System.currentTimeMillis()
        val existing = inventoryDao.getAll().map { it.toDomain() }
        val progress = prefs.progressFlow.first()
        var inv = InventoryLogic.ensureKinds(existing, now)
        // 遷移舊 SEED_BAG → SEED_WHEAT（fromStorage 已處理 kind；合併同 kind）
        inv = mergeInventoryByKind(inv, now)
        val seedTotal = InventoryLogic.totalSeeds(inv)
        if (seedTotal == 0 && progress.seeds > 0) {
            inv = InventoryLogic.add(inv, InventoryItemKind.SEED_WHEAT, progress.seeds, now).inventory
        } else if (existing.isEmpty() && seedTotal == 0) {
            // 新玩家起步種子
            inv = InventoryLogic.add(inv, InventoryItemKind.SEED_WHEAT, 3, now).inventory
        }
        // M3：首次建立背包時贈 2 袋飼料（唔發成長點）；已有 FEED 列則不重贈
        val hadFeedRow = existing.any { it.kind == InventoryItemKind.FEED }
        if (!hadFeedRow && InventoryLogic.feedQty(inv) == 0) {
            inv = InventoryLogic.add(inv, InventoryItemKind.FEED, 2, now).inventory
        }
        inventoryDao.upsertAll(inv.map { it.toEntity() })
    }

    private fun mergeInventoryByKind(items: List<InventoryItem>, now: Long): List<InventoryItem> {
        val sums = linkedMapOf<InventoryItemKind, Int>()
        InventoryItemKind.entries.forEach { sums[it] = 0 }
        items.forEach { sums[it.kind] = (sums[it.kind] ?: 0) + it.quantity.coerceAtLeast(0) }
        return InventoryItemKind.entries.map { kind ->
            InventoryItem(
                id = kind.name.lowercase(),
                kind = kind,
                quantity = sums.getValue(kind),
                updatedAtEpochMs = now
            )
        }
    }

    private suspend fun loadInventory(now: Long = System.currentTimeMillis()): List<InventoryItem> {
        val raw = inventoryDao.getAll().map { it.toDomain() }
        return mergeInventoryByKind(InventoryLogic.ensureKinds(raw, now), now)
    }

    private suspend fun saveInventory(items: List<InventoryItem>) {
        inventoryDao.upsertAll(items.map { it.toEntity() })
    }

    suspend fun clearClockPause() {
        val today = LocalDate.now().toString()
        val p = prefs.progressFlow.first()
        prefs.saveProgress(DailySettlementLogic.clearClockPause(p, today))
    }

    suspend fun completeOnboarding() {
        val p = prefs.progressFlow.first()
        prefs.saveProgress(p.copy(onboardingDone = true))
    }

    suspend fun addEntry(
        type: EntryType,
        amountMinor: Long,
        note: String,
        localDate: String = LocalDate.now().toString()
    ): LedgerEntry {
        val now = System.currentTimeMillis()
        val amt = if (type == EntryType.NO_TRADE) 0L else amountMinor.coerceAtLeast(0)
        val entry = LedgerEntry(
            id = UUID.randomUUID().toString(),
            localDate = localDate,
            type = type,
            amountMinor = amt,
            note = note,
            status = EntryStatus.ACTIVE,
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )
        ledgerDao.upsert(entry.toEntity())
        return entry
    }

    suspend fun updateEntry(id: String, type: EntryType, amountMinor: Long, note: String): Boolean {
        val existing = ledgerDao.getById(id) ?: return false
        if (existing.status == EntryStatus.VOIDED.name) return false
        val amt = if (type == EntryType.NO_TRADE) 0L else amountMinor.coerceAtLeast(0)
        ledgerDao.update(
            existing.copy(
                type = type.name,
                amountMinor = amt,
                note = note,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
        // 編輯不觸發再次結算／發獎
        return true
    }

    suspend fun voidEntry(id: String): Boolean {
        val existing = ledgerDao.getById(id) ?: return false
        ledgerDao.update(
            existing.copy(
                status = EntryStatus.VOIDED.name,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun settleToday(): SettlementResult {
        val today = LocalDate.now().toString()
        syncClock(today)
        val progress = prefs.progressFlow.first()
        val activeCount = ledgerDao.activeCount(today)
        val result = DailySettlementLogic.settle(
            progress = progress,
            today = today,
            nowEpochMs = System.currentTimeMillis(),
            hasAnyLedgerActivity = activeCount > 0
        )
        if (result.awarded && result.settlement != null) {
            // IGNORE：資料庫層防雙重發獎
            val inserted = settlementDao.insertIgnore(result.settlement.toEntity())
            if (inserted == -1L) {
                // 已存在 → 不更新 progress 獎勵
                return result.copy(
                    awarded = false,
                    settlement = null,
                    messageZh = "今日已結算過（資料庫防重）。"
                )
            }
            prefs.saveProgress(result.progress)
            // 連續結算種子加成入背包（唔發成長點）
            val seedDelta = result.progress.seeds - progress.seeds
            if (seedDelta > 0) {
                val now = System.currentTimeMillis()
                val inv = InventoryLogic.grantStreakSeeds(loadInventory(now), seedDelta, now)
                saveInventory(inv)
            }
            // 解鎖裝飾：連續 3/5/7
            unlockDecorForStreak(result.progress)
        }
        return result
    }

    private suspend fun unlockDecorForStreak(progress: PlayerProgress) {
        val decors = prefs.decorationsFlow.first().toMutableList()
        fun unlock(id: String) {
            val i = decors.indexOfFirst { it.id == id }
            if (i >= 0) decors[i] = decors[i].copy(unlocked = true)
        }
        if (3 in progress.unlockedStreakRewards) unlock("scarecrow")
        if (5 in progress.unlockedStreakRewards) { unlock("lantern"); unlock("bench") }
        if (7 in progress.unlockedStreakRewards) { unlock("well"); unlock("flowerbed"); unlock("windchime") }
        prefs.saveDecorations(decors)
    }

    suspend fun claimWeeklyReview(): WeeklyReviewResult {
        val today = LocalDate.now()
        syncClock(today.toString())
        val progress = prefs.progressFlow.first()
        val weekFields = WeekFields.of(Locale.TAIWAN)
        val weekId = "${today.year}-W${today.get(weekFields.weekOfWeekBasedYear()).toString().padStart(2, '0')}"
        val start = today.with(weekFields.dayOfWeek(), 1)
        val end = start.plusDays(6)
        val settled = settlementDao.getBetween(start.toString(), end.toString()).size
        val result = WeeklyReviewLogic.claim(progress, weekId, settled)
        if (result.awardedPoints > 0) prefs.saveProgress(result.progress)
        return result
    }

    suspend fun plant(plotIndex: Int, crop: CropKind): String {
        syncClock()
        ensureInventoryStubs()
        val now = System.currentTimeMillis()
        val progress = prefs.progressFlow.first()
        val plots = FarmLogic.refreshPlots(prefs.plotsFlow.first(), now, progress.clockPaused)
        val inv = loadInventory(now)
        val r = FarmLogic.plant(plots, progress, inv, plotIndex, crop, now)
        if (r.ok) {
            prefs.savePlots(r.plots)
            saveInventory(r.inventory)
            // 同步舊 seeds 顯示欄（以背包總種子為準）
            prefs.saveProgress(r.progress.copy(seeds = InventoryLogic.totalSeeds(r.inventory)))
        }
        return r.msg
    }

    suspend fun water(plotIndex: Int): String {
        syncClock()
        val now = System.currentTimeMillis()
        val progress = prefs.progressFlow.first()
        val plots = FarmLogic.refreshPlots(prefs.plotsFlow.first(), now, progress.clockPaused)
        val r = FarmLogic.water(plots, progress, plotIndex, now)
        if (r.ok) prefs.savePlots(r.plots)
        return r.msg
    }

    suspend fun harvest(plotIndex: Int): String {
        syncClock()
        ensureInventoryStubs()
        val now = System.currentTimeMillis()
        val progress = prefs.progressFlow.first()
        val plots = FarmLogic.refreshPlots(prefs.plotsFlow.first(), now, progress.clockPaused)
        val inv = loadInventory(now)
        val r = FarmLogic.harvest(plots, progress, inv, plotIndex, now)
        if (r.ok) {
            prefs.savePlots(r.plots)
            saveInventory(r.inventory)
            prefs.saveProgress(r.progress.copy(seeds = InventoryLogic.totalSeeds(r.inventory)))
        } else {
            prefs.savePlots(plots)
        }
        return r.msg
    }

    /**
     * 買種子：經背包入庫並寫支出帳。金額／筆數唔發成長點。
     */
    suspend fun buySeeds(crop: CropKind, quantity: Int = 1): String {
        syncClock()
        ensureInventoryStubs()
        val now = System.currentTimeMillis()
        val inv = loadInventory(now)
        val trade = InventoryLogic.buySeeds(inv, crop, quantity, now)
        if (!trade.ok || trade.entryType == null) return trade.msg
        saveInventory(trade.inventory)
        val progress = prefs.progressFlow.first()
        prefs.saveProgress(progress.copy(seeds = InventoryLogic.totalSeeds(trade.inventory)))
        addEntry(trade.entryType, trade.amountMinor, trade.note)
        return trade.msg
    }

    /**
     * 賣收成：經背包出庫並寫收入帳。金額／筆數唔發成長點。
     */
    suspend fun sellHarvest(crop: CropKind, quantity: Int = 1): String {
        syncClock()
        ensureInventoryStubs()
        val now = System.currentTimeMillis()
        val inv = loadInventory(now)
        val trade = InventoryLogic.sellHarvest(inv, crop, quantity, now)
        if (!trade.ok || trade.entryType == null) return trade.msg
        saveInventory(trade.inventory)
        addEntry(trade.entryType, trade.amountMinor, trade.note)
        return trade.msg
    }

    suspend fun buyFeed(quantity: Int = 1): String {
        ensureInventoryStubs()
        syncClock()
        val now = System.currentTimeMillis()
        val trade = InventoryLogic.buyFeed(loadInventory(now), quantity, now)
        if (!trade.ok || trade.entryType == null) return trade.msg
        saveInventory(trade.inventory)
        addEntry(trade.entryType, trade.amountMinor, trade.note)
        return trade.msg
    }


    suspend fun refreshFarm() {
        val progress = prefs.progressFlow.first()
        val plots = FarmLogic.refreshPlots(prefs.plotsFlow.first(), System.currentTimeMillis(), progress.clockPaused)
        prefs.savePlots(plots)
    }

    suspend fun feedPet(): String {
        ensureInventoryStubs()
        syncClock()
        val progress = prefs.progressFlow.first()
        val pet = prefs.petFlow.first()
        val now = System.currentTimeMillis()
        val r = FarmLogic.feedPet(pet, progress, loadInventory(now), now)
        if (r.ok) {
            // 成長點不變；只存寵物＋背包
            r.pet?.let { prefs.savePet(it) }
            saveInventory(r.inventory)
        }
        return r.msg
    }

    /** 場景內佈置：扣成長點後解鎖並放置一件尚未放置的裝飾。 */
    suspend fun buildDecorInScene(): String {
        syncClock()
        val progress = prefs.progressFlow.first()
        val decors = prefs.decorationsFlow.first().toMutableList()
        val placedCount = decors.count { it.placed }
        val r = FarmLogic.buildDecor(progress, placedCount)
        if (!r.ok) return r.msg
        // Prefer unlocked-but-unplaced; else unlock first locked within slot
        val targetIdx = decors.indexOfFirst { it.unlocked && !it.placed }
            .takeIf { it >= 0 }
            ?: decors.indexOfFirst { !it.unlocked }.takeIf { it >= 0 }
        if (targetIdx == null) {
            return "沒有可佈置的裝飾。"
        }
        val d = decors[targetIdx]
        decors[targetIdx] = d.copy(unlocked = true, placed = true)
        prefs.saveProgress(r.progress)
        prefs.saveDecorations(decors)
        return r.msg + "（${decors[targetIdx].nameZh}）"
    }

    suspend fun settlementCount(): Int = settlementDao.countAll()

    suspend fun interactPet(): String {
        syncClock()
        val progress = prefs.progressFlow.first()
        val pet = prefs.petFlow.first()
        val (updated, msg) = FarmLogic.interactPet(pet, System.currentTimeMillis(), progress.clockPaused)
        if (msg.contains("成功")) prefs.savePet(updated)
        return msg
    }

    suspend fun renamePet(name: String) {
        val pet = prefs.petFlow.first()
        prefs.savePet(pet.copy(name = name.ifBlank { pet.name }))
    }

    suspend fun toggleDecorationPlaced(id: String) {
        val list = prefs.decorationsFlow.first().map {
            if (it.id == id && it.unlocked) it.copy(placed = !it.placed) else it
        }
        prefs.saveDecorations(list)
    }

    suspend fun exportJson(): String {
        val dump = ExportBundle(
            entries = ledgerDao.getAll().map { it.toDomain() },
            settlements = settlementDao.getAll().map { it.toDomain() },
            progress = prefs.progressFlow.first(),
            plots = prefs.plotsFlow.first(),
            pet = prefs.petFlow.first(),
            decorations = prefs.decorationsFlow.first(),
            gameDay = ensureGameDay(),
            inventory = inventoryDao.getAll().map { it.toDomain() }
        )
        return json.encodeToString(dump)
    }

    suspend fun exportCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("id,localDate,type,amountMinor,note,status,createdAtEpochMs,updatedAtEpochMs")
        ledgerDao.getAll().forEach { e ->
            sb.appendLine(
                listOf(
                    e.id, e.localDate, e.type, e.amountMinor,
                    "\"${e.note.replace("\"", "\"\"")}\"", e.status,
                    e.createdAtEpochMs, e.updatedAtEpochMs
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    suspend fun importJson(text: String): String {
        return try {
            val dump = json.decodeFromString<ExportBundle>(text)
            ledgerDao.upsertAll(dump.entries.map { it.toEntity() })
            settlementDao.upsertAll(dump.settlements.map { it.toEntity() })
            prefs.saveProgress(dump.progress)
            prefs.savePlots(dump.plots.ifEmpty { FarmLogic.defaultPlots() })
            prefs.savePet(dump.pet)
            prefs.saveDecorations(dump.decorations.ifEmpty { GamePreferences.defaultDecorations() })
            gameDayDao.upsert(dump.gameDay.toEntity())
            if (dump.inventory.isNotEmpty()) {
                inventoryDao.clear()
                inventoryDao.upsertAll(dump.inventory.map { it.toEntity() })
            }
            "匯入成功：${dump.entries.size} 筆帳目。"
        } catch (e: Exception) {
            "匯入失敗：${e.message}"
        }
    }

    suspend fun importCsv(text: String): String {
        return try {
            val lines = text.lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) return "空檔案"
            val dataLines = if (lines.first().startsWith("id,")) lines.drop(1) else lines
            val entries = dataLines.mapNotNull { line ->
                val parts = parseCsvLine(line)
                if (parts.size < 8) return@mapNotNull null
                LedgerEntryEntity(
                    id = parts[0],
                    localDate = parts[1],
                    type = parts[2],
                    amountMinor = parts[3].toLongOrNull() ?: 0L,
                    note = parts[4],
                    status = parts[5],
                    createdAtEpochMs = parts[6].toLongOrNull() ?: 0L,
                    updatedAtEpochMs = parts[7].toLongOrNull() ?: 0L
                )
            }
            ledgerDao.upsertAll(entries)
            "CSV 匯入成功：${entries.size} 筆。"
        } catch (e: Exception) {
            "CSV 匯入失敗：${e.message}"
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    sb.append('"'); i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result += sb.toString(); sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        result += sb.toString()
        return result
    }
}

@Serializable
data class ExportBundle(
    val entries: List<LedgerEntry> = emptyList(),
    val settlements: List<DailySettlement> = emptyList(),
    val progress: PlayerProgress = PlayerProgress(),
    val plots: List<Plot> = emptyList(),
    val pet: PetState = PetState(),
    val decorations: List<Decoration> = emptyList(),
    val gameDay: GameDayState = GameDayState(),
    val inventory: List<InventoryItem> = emptyList()
)

private fun LedgerEntryEntity.toDomain() = LedgerEntry(
    id, localDate, EntryType.valueOf(type), amountMinor, note,
    EntryStatus.valueOf(status), createdAtEpochMs, updatedAtEpochMs
)

private fun LedgerEntry.toEntity() = LedgerEntryEntity(
    id, localDate, type.name, amountMinor, note, status.name, createdAtEpochMs, updatedAtEpochMs
)

private fun DailySettlementEntity.toDomain() = DailySettlement(localDate, growthPointsAwarded, settledAtEpochMs)
private fun DailySettlement.toEntity() = DailySettlementEntity(localDate, growthPointsAwarded, settledAtEpochMs)


private fun GameDayStateEntity.toDomain() = GameDayState(
    gameDay = gameDay,
    phase = DayPhase.fromName(phase),
    farmStageHint = farmStageHint,
    lastWallClockEpochMs = lastWallClockEpochMs,
    updatedAtEpochMs = updatedAtEpochMs
)

private fun GameDayState.toEntity() = GameDayStateEntity(
    id = 1,
    gameDay = gameDay,
    phase = phase.name,
    farmStageHint = farmStageHint,
    lastWallClockEpochMs = lastWallClockEpochMs,
    updatedAtEpochMs = updatedAtEpochMs
)

private fun InventoryItemEntity.toDomain() = InventoryItem(
    id = id,
    kind = InventoryItemKind.fromStorage(kind),
    quantity = quantity,
    updatedAtEpochMs = updatedAtEpochMs
)

private fun InventoryItem.toEntity() = InventoryItemEntity(
    id = id,
    kind = kind.name,
    quantity = quantity,
    updatedAtEpochMs = updatedAtEpochMs
)
