package com.farmledger.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.farmledger.app.domain.model.Decoration
import com.farmledger.app.domain.model.PetState
import com.farmledger.app.domain.model.PlayerProgress
import com.farmledger.app.domain.model.Plot
import com.farmledger.app.domain.usecase.FarmLogic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore("game_prefs")

class GamePreferences(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val progressKey = stringPreferencesKey("progress")
    private val plotsKey = stringPreferencesKey("plots")
    private val petKey = stringPreferencesKey("pet")
    private val decorKey = stringPreferencesKey("decorations")

    val progressFlow: Flow<PlayerProgress> = context.gameDataStore.data.map { prefs ->
        prefs[progressKey]?.let { runCatching { json.decodeFromString<PlayerProgress>(it) }.getOrNull() }
            ?: PlayerProgress()
    }

    val plotsFlow: Flow<List<Plot>> = context.gameDataStore.data.map { prefs ->
        prefs[plotsKey]?.let { runCatching { json.decodeFromString<List<Plot>>(it) }.getOrNull() }
            ?: FarmLogic.defaultPlots()
    }

    val petFlow: Flow<PetState> = context.gameDataStore.data.map { prefs ->
        prefs[petKey]?.let { runCatching { json.decodeFromString<PetState>(it) }.getOrNull() }
            ?: PetState()
    }

    val decorationsFlow: Flow<List<Decoration>> = context.gameDataStore.data.map { prefs ->
        prefs[decorKey]?.let { runCatching { json.decodeFromString<List<Decoration>>(it) }.getOrNull() }
            ?: defaultDecorations()
    }

    suspend fun saveProgress(p: PlayerProgress) {
        context.gameDataStore.edit { it[progressKey] = json.encodeToString(p) }
    }

    suspend fun savePlots(plots: List<Plot>) {
        context.gameDataStore.edit { it[plotsKey] = json.encodeToString(plots) }
    }

    suspend fun savePet(pet: PetState) {
        context.gameDataStore.edit { it[petKey] = json.encodeToString(pet) }
    }

    suspend fun saveDecorations(list: List<Decoration>) {
        context.gameDataStore.edit { it[decorKey] = json.encodeToString(list) }
    }

    companion object {
        fun defaultDecorations(): List<Decoration> = listOf(
            Decoration("fence", "木柵欄", unlocked = true, placed = false),
            Decoration("scarecrow", "稻草人", unlocked = false),
            Decoration("lantern", "暖黃燈籠", unlocked = false),
            Decoration("well", "石井", unlocked = false),
            Decoration("bench", "長椅", unlocked = false),
            Decoration("flowerbed", "花圃", unlocked = false),
            Decoration("windchime", "風鈴", unlocked = false)
        )
    }
}
