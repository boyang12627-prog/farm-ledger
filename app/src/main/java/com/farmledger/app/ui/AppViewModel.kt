package com.farmledger.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmledger.app.data.repository.FarmLedgerRepository
import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.GameDayState
import com.farmledger.app.domain.model.InventoryItem
import com.farmledger.app.domain.model.Decoration
import com.farmledger.app.domain.model.EntryType
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
        category: String? = null
    ) = viewModelScope.launch {
        repo.addEntry(type, amountMinor, note, _selectedDate.value, category = category)
        _message.value = "已新增港幣帳目"
    }

    fun updateEntry(
        id: String,
        type: EntryType,
        amountMinor: Long,
        note: String,
        category: String? = null
    ) = viewModelScope.launch {
        val ok = repo.updateEntry(id, type, amountMinor, note, category = category)
        _message.value = if (ok) "已更新（不會再次發放成長點／種子幣）" else "更新失敗"
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
