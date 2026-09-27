package com.example.cornerrace.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cornerrace.data.AppSettings
import com.example.cornerrace.data.BoardTheme
import com.example.cornerrace.data.GameStats
import com.example.cornerrace.data.PreferencesRepository
import com.example.cornerrace.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Activity-scoped settings and statistics shared by every screen. */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository(application)

    val settings: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val stats: StateFlow<GameStats> =
        repository.stats.stateIn(viewModelScope, SharingStarted.Eagerly, GameStats())

    fun setThemeMode(mode: ThemeMode) = launch { repository.setThemeMode(mode) }
    fun setBoardTheme(theme: BoardTheme) = launch { repository.setBoardTheme(theme) }
    fun setSound(enabled: Boolean) = launch { repository.setSound(enabled) }
    fun setHaptics(enabled: Boolean) = launch { repository.setHaptics(enabled) }
    fun setShowLegalMoves(enabled: Boolean) = launch { repository.setShowLegalMoves(enabled) }
    fun setShowTargetZones(enabled: Boolean) = launch { repository.setShowTargetZones(enabled) }
    fun resetStats() = launch { repository.resetStats() }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
