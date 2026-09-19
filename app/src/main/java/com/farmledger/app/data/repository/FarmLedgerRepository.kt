package com.farmledger.app.data.repository

import com.farmledger.app.data.local.dao.LedgerDao
import com.farmledger.app.data.local.dao.SettlementDao
import com.farmledger.app.data.local.datastore.GamePreferences
import com.farmledger.app.data.local.entity.DailySettlementEntity
import com.farmledger.app.data.local.entity.LedgerEntryEntity
import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.DailySettlement
import com.farmledger.app.domain.model.Decoration
import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerEntry
import com.farmledger.app.domain.model.PetState
import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.Plot
import com.farmledger.app.domain.usecase.DailySettlementLogic
import com.farmledger.app.domain.usecase.FarmLogic
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
    private val prefs: GamePreferences
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }

    val progressFlow: Flow<PlayerProgress> = prefs.progressFlow
    val plotsFlow: Flow<List<Plot>> = prefs.plotsFlow
    val petFlow: Flow<PetState> = prefs.petFlow
    val decorationsFlow: Flow<List<Decoration>> = prefs.decorationsFlow

    fun observeEntries(date: String): Flow<List<LedgerEntry>> =
        ledgerDao.observeByDate(date).map { list -> list.map { it.toDomain() } }

    suspend fun syncClock(today: String = LocalDate.now().toString()) {
        val p = prefs.progressFlow.first()
        val updated = DailySettlementLogic.detectClockRegression(p, today)
        if (updated != p) prefs.saveProgress(updated)
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
        val progress = prefs.progressFlow.first()
        val plots = FarmLogic.refreshPlots(prefs.plotsFlow.first(), System.currentTimeMillis(), progress.clockPaused)
        val r = FarmLogic.plant(plots, progress, plotIndex, crop, System.currentTimeMillis())
        if (r.ok) {
            prefs.savePlots(r.plots)
            prefs.saveProgress(r.progress)
        }
        return r.msg
    }

    suspend fun harvest(plotIndex: Int): String {
        syncClock()
        val progress = prefs.progressFlow.first()
        val plots = FarmLogic.refreshPlots(prefs.plotsFlow.first(), System.currentTimeMillis(), progress.clockPaused)
        val r = FarmLogic.harvest(plots, progress, plotIndex, System.currentTimeMillis())
        if (r.ok) {
            prefs.savePlots(r.plots)
            prefs.saveProgress(r.progress)
        } else {
            prefs.savePlots(plots)
        }
        return r.msg
    }

    suspend fun refreshFarm() {
        val progress = prefs.progressFlow.first()
        val plots = FarmLogic.refreshPlots(prefs.plotsFlow.first(), System.currentTimeMillis(), progress.clockPaused)
        prefs.savePlots(plots)
    }

    suspend fun feedPet(): String {
        syncClock()
        val progress = prefs.progressFlow.first()
        val pet = prefs.petFlow.first()
        val (updated, msg) = FarmLogic.feedPet(pet, System.currentTimeMillis(), progress.clockPaused)
        if (msg.contains("成功")) prefs.savePet(updated)
        return msg
    }

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
            decorations = prefs.decorationsFlow.first()
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
    val decorations: List<Decoration> = emptyList()
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
