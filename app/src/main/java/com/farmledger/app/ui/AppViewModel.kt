package com.farmledger.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmledger.app.data.repository.FarmLedgerRepository
import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.GameDayState
import com.farmledger.app.domain.model.InventoryItem
import com.farmledger.app.domain.model.Decoration
import com.farmledger.app.domain.model.DefaultAccounts
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerAccount
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry
import com.farmledger.app.domain.model.PetState
import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.Plot
import com.farmledger.app.domain.usecase.FarmLogic
import com.farmledger.app.domain.usecase.DayPhaseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class AppViewModel(private val repo: FarmLedgerRepository) : ViewModel() {

    val progress: StateFlow<PlayerProgress> = repo.progressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerProgress())

    val plots: StateFlow<List<Plot>> = repo.plotsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FarmLogic.defaultPlots())

    val pet: StateFlow<PetState> = repo.petFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PetState())

    val decorations: StateFlow<List<Decoration>> = repo.decorationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gameDay: StateFlow<GameDayState> = repo.gameDayFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameDayState())

    /** M2 背包：種子／收成堆疊 */
    val inventory: StateFlow<List<InventoryItem>> = repo.inventoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** M5.1：真港幣帳戶（預設現金＋自訂） */
    val accounts: StateFlow<List<LedgerAccount>> = repo.accountsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(DefaultAccounts.CASH))

    /** 牧場熱區 Tap → 入帳預填分類（消費後清空） */
    private val _pendingEntryCategory = MutableStateFlow<LedgerCategory?>(null)
    val pendingEntryCategory: StateFlow<LedgerCategory?> = _pendingEntryCategory.asStateFlow()

    private val _entries = MutableStateFlow<List<LedgerEntry>>(emptyList())
    val entries: StateFlow<List<LedgerEntry>> = _entries.asStateFlow()

    private val _allEntries = MutableStateFlow<List<LedgerEntry>>(emptyList())
    val allEntries: StateFlow<List<LedgerEntry>> = _allEntries.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /** M3：餵食／互動成功後場景開心／進食反饋截止時間 */
    private val _petFeedbackUntilMs = MutableStateFlow(0L)
    val petFeedbackUntilMs: StateFlow<Long> = _petFeedbackUntilMs.asStateFlow()

    /** M3：結算儀式——剛成功發獎時供 overlay 播月亮／蓋章 */
    private val _settleCeremonyAwarded = MutableStateFlow(false)
    val settleCeremonyAwarded: StateFlow<Boolean> = _settleCeremonyAwarded.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    init {
        viewModelScope.launch {
            repo.ensureInventoryStubs()
            launch {
                repo.observeAllEntries().collect { _allEntries.value = it }
            }
            _selectedDate.collect { date ->
                repo.observeEntries(date).collect { _entries.value = it }
            }
        }
    }

    fun syncClock() = viewModelScope.launch { repo.syncClock() }
    fun clearClockPause() = viewModelScope.launch { repo.clearClockPause() }
    fun completeOnboarding() = viewModelScope.launch { repo.completeOnboarding() }

    fun setDate(date: String) { _selectedDate.value = date }

    fun addEntry(
        type: EntryType,
        amountMinor: Long,
        note: String,
        category: String? = null,
        accountId: String = DefaultAccounts.CASH_ID,
        transferAccountId: String? = null
    ) = viewModelScope.launch {
        if (type == EntryType.TRANSFER) {
            val from = accountId.ifBlank { DefaultAccounts.CASH_ID }
            val to = transferAccountId?.takeIf { it.isNotBlank() }
            if (to == null || to == from) {
                _message.value = "轉帳請揀唔同嘅由帳戶／到帳戶"
                return@launch
            }
            if (amountMinor <= 0L) {
                _message.value = "轉帳金額要大於 0"
                return@launch
            }
        }
        try {
            repo.addEntry(
                type = type,
                amountMinor = amountMinor,
                note = note,
                localDate = _selectedDate.value,
                category = category,
                accountId = accountId,
                transferAccountId = transferAccountId
            )
            _message.value = when (type) {
                EntryType.TRANSFER -> "已記轉帳"
                else -> "已新增港幣帳目"
            }
        } catch (e: Exception) {
            _message.value = e.message ?: "儲存失敗"
        }
    }

    fun updateEntry(
        id: String,
        type: EntryType,
        amountMinor: Long,
        note: String,
        category: String? = null,
        accountId: String? = null,
        transferAccountId: String? = null
    ) = viewModelScope.launch {
        if (type == EntryType.TRANSFER) {
            val from = accountId
            val to = transferAccountId
            if (from != null && to != null && from == to) {
                _message.value = "轉帳請揀唔同嘅由帳戶／到帳戶"
                return@launch
            }
        }
        val ok = repo.updateEntry(
            id, type, amountMinor, note,
            category = category,
            accountId = accountId,
            transferAccountId = transferAccountId
        )
        _message.value = if (ok) "已更新（不會再次發放成長點／種子幣）" else "更新失敗"
    }

    fun addAccount(nameZh: String) = viewModelScope.launch {
        val a = repo.addAccount(nameZh)
        _message.value = "已新增帳戶「${a.nameZh}」"
    }

    fun renameAccount(id: String, nameZh: String) = viewModelScope.launch {
        val ok = repo.renameAccount(id, nameZh)
        _message.value = if (ok) "已改名" else "改名失敗"
    }

    fun prefillEntryCategory(category: LedgerCategory) {
        _pendingEntryCategory.value = category
    }

    fun consumePendingEntryCategory(): LedgerCategory? {
        val c = _pendingEntryCategory.value
        _pendingEntryCategory.value = null
        return c
    }

    fun voidEntry(id: String) = viewModelScope.launch {
        repo.voidEntry(id)
        _message.value = "已作廢"
    }

    fun settleToday() = viewModelScope.launch {
        val r = repo.settleToday()
        _message.value = r.messageZh
        _settleCeremonyAwarded.value = r.awarded
    }

    fun claimWeekly() = viewModelScope.launch {
        val r = repo.claimWeeklyReview()
        _message.value = r.messageZh
    }

    fun plant(index: Int, crop: CropKind) = viewModelScope.launch {
        _message.value = repo.plant(index, crop)
    }

    fun water(index: Int) = viewModelScope.launch {
        _message.value = repo.water(index)
    }

    fun harvest(index: Int) = viewModelScope.launch {
        _message.value = repo.harvest(index)
    }

    fun buySeeds(crop: CropKind, quantity: Int = 1) = viewModelScope.launch {
        _message.value = repo.buySeeds(crop, quantity)
    }

    fun sellHarvest(crop: CropKind, quantity: Int = 1) = viewModelScope.launch {
        _message.value = repo.sellHarvest(crop, quantity)
    }

    fun buyFeed(quantity: Int = 1) = viewModelScope.launch {
        _message.value = repo.buyFeed(quantity)
    }

    fun refreshFarm() = viewModelScope.launch { repo.refreshFarm() }

    fun feedPet() = viewModelScope.launch {
        val msg = repo.feedPet()
        _message.value = msg
        if (msg.contains("餵食成功")) {
            _petFeedbackUntilMs.value = System.currentTimeMillis() + 1_600L
        }
    }
    fun interactPet() = viewModelScope.launch {
        val msg = repo.interactPet()
        _message.value = msg
        if (msg.contains("成功")) {
            _petFeedbackUntilMs.value = System.currentTimeMillis() + 1_400L
        }
    }
    fun renamePet(name: String) = viewModelScope.launch { repo.renamePet(name) }

    fun toggleDecor(id: String) = viewModelScope.launch { repo.toggleDecorationPlaced(id) }
    fun buildDecorInScene() = viewModelScope.launch { _message.value = repo.buildDecorInScene() }

    suspend fun exportJson(): String = repo.exportJson()
    suspend fun exportCsv(): String = repo.exportCsv()
    fun importJson(text: String) = viewModelScope.launch { _message.value = repo.importJson(text) }
    fun importCsv(text: String) = viewModelScope.launch { _message.value = repo.importCsv(text) }

    fun waitNextPhase() = viewModelScope.launch {
        val r: DayPhaseResult = repo.waitNextPhase()
        _message.value = r.messageZh
    }

    fun sleepToNextDay() = viewModelScope.launch {
        val r: DayPhaseResult = repo.sleepToNextDay()
        _message.value = r.messageZh
    }

    fun consumeMessage() { _message.value = null }
    fun consumeSettleCeremony() { _settleCeremonyAwarded.value = false }

    fun entryById(id: String): LedgerEntry? =
        _allEntries.value.find { it.id == id } ?: _entries.value.find { it.id == id }
}

class AppViewModelFactory(private val repo: FarmLedgerRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            return AppViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
