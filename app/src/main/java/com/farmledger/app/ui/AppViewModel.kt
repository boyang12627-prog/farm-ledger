package com.farmledger.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.farmledger.app.data.repository.FarmLedgerRepository
import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.Decoration
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerEntry
import com.farmledger.app.domain.model.PetState
import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.Plot
import com.farmledger.app.domain.usecase.FarmLogic
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

    private val _entries = MutableStateFlow<List<LedgerEntry>>(emptyList())
    val entries: StateFlow<List<LedgerEntry>> = _entries.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedDate.collect { date ->
                repo.observeEntries(date).collect { _entries.value = it }
            }
        }
    }

    fun syncClock() = viewModelScope.launch { repo.syncClock() }
    fun clearClockPause() = viewModelScope.launch { repo.clearClockPause() }
    fun completeOnboarding() = viewModelScope.launch { repo.completeOnboarding() }

    fun setDate(date: String) { _selectedDate.value = date }

    fun addEntry(type: EntryType, amountMinor: Long, note: String) = viewModelScope.launch {
        repo.addEntry(type, amountMinor, note, _selectedDate.value)
        _message.value = "已新增帳目"
    }

    fun updateEntry(id: String, type: EntryType, amountMinor: Long, note: String) =
        viewModelScope.launch {
            val ok = repo.updateEntry(id, type, amountMinor, note)
            _message.value = if (ok) "已更新（不會再次發放成長點）" else "更新失敗"
        }

    fun voidEntry(id: String) = viewModelScope.launch {
        repo.voidEntry(id)
        _message.value = "已作廢"
    }

    fun settleToday() = viewModelScope.launch {
        val r = repo.settleToday()
        _message.value = r.messageZh
    }

    fun claimWeekly() = viewModelScope.launch {
        val r = repo.claimWeeklyReview()
        _message.value = r.messageZh
    }

    fun plant(index: Int, crop: CropKind) = viewModelScope.launch {
        _message.value = repo.plant(index, crop)
    }

    fun harvest(index: Int) = viewModelScope.launch {
        _message.value = repo.harvest(index)
    }

    fun refreshFarm() = viewModelScope.launch { repo.refreshFarm() }

    fun feedPet() = viewModelScope.launch { _message.value = repo.feedPet() }
    fun interactPet() = viewModelScope.launch { _message.value = repo.interactPet() }
    fun renamePet(name: String) = viewModelScope.launch { repo.renamePet(name) }

    fun toggleDecor(id: String) = viewModelScope.launch { repo.toggleDecorationPlaced(id) }
    fun buildDecorInScene() = viewModelScope.launch { _message.value = repo.buildDecorInScene() }

    suspend fun exportJson(): String = repo.exportJson()
    suspend fun exportCsv(): String = repo.exportCsv()
    fun importJson(text: String) = viewModelScope.launch { _message.value = repo.importJson(text) }
    fun importCsv(text: String) = viewModelScope.launch { _message.value = repo.importCsv(text) }

    fun consumeMessage() { _message.value = null }

    fun entryById(id: String): LedgerEntry? = _entries.value.find { it.id == id }
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
