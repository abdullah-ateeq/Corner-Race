package com.example.cornerrace.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cornerrace.game.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val label: String) { LIGHT("Light"), DARK("Dark"), SYSTEM("System") }

enum class BoardTheme(val label: String) { CLASSIC("Classic"), MIDNIGHT("Midnight") }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val showLegalMoves: Boolean = true,
    val showTargetZones: Boolean = true
)

data class GameStats(
    val gamesPlayed: Int = 0,
    val player1Wins: Int = 0,
    val player2Wins: Int = 0,
    val fewestTurnsToWin: Int? = null
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "corner_race")

class PreferencesRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val BOARD = stringPreferencesKey("board_theme")
        val SOUND = booleanPreferencesKey("sound")
        val HAPTICS = booleanPreferencesKey("haptics")
        val LEGAL = booleanPreferencesKey("show_legal_moves")
        val ZONES = booleanPreferencesKey("show_target_zones")
        val GAMES = intPreferencesKey("games_played")
        val P1_WINS = intPreferencesKey("p1_wins")
        val P2_WINS = intPreferencesKey("p2_wins")
        val FEWEST = intPreferencesKey("fewest_turns")
    }

    val settings: Flow<AppSettings> = store.data.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME].toEnum(ThemeMode.SYSTEM),
            boardTheme = p[Keys.BOARD].toEnum(BoardTheme.CLASSIC),
            soundEnabled = p[Keys.SOUND] ?: true,
            hapticsEnabled = p[Keys.HAPTICS] ?: true,
            showLegalMoves = p[Keys.LEGAL] ?: true,
            showTargetZones = p[Keys.ZONES] ?: true
        )
    }

    val stats: Flow<GameStats> = store.data.map { p ->
        GameStats(
            gamesPlayed = p[Keys.GAMES] ?: 0,
            player1Wins = p[Keys.P1_WINS] ?: 0,
            player2Wins = p[Keys.P2_WINS] ?: 0,
            fewestTurnsToWin = p[Keys.FEWEST]
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = store.edit { it[Keys.THEME] = mode.name }
    suspend fun setBoardTheme(theme: BoardTheme) = store.edit { it[Keys.BOARD] = theme.name }
    suspend fun setSound(enabled: Boolean) = store.edit { it[Keys.SOUND] = enabled }
    suspend fun setHaptics(enabled: Boolean) = store.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setShowLegalMoves(enabled: Boolean) = store.edit { it[Keys.LEGAL] = enabled }
    suspend fun setShowTargetZones(enabled: Boolean) = store.edit { it[Keys.ZONES] = enabled }

    suspend fun recordWin(winner: Player, turns: Int) = store.edit { p ->
        p[Keys.GAMES] = (p[Keys.GAMES] ?: 0) + 1
        val key = if (winner == Player.PLAYER_ONE) Keys.P1_WINS else Keys.P2_WINS
        p[key] = (p[key] ?: 0) + 1
        val fewest = p[Keys.FEWEST]
        if (fewest == null || turns < fewest) p[Keys.FEWEST] = turns
    }

    suspend fun resetStats() = store.edit { p ->
        p.remove(Keys.GAMES)
        p.remove(Keys.P1_WINS)
        p.remove(Keys.P2_WINS)
        p.remove(Keys.FEWEST)
    }

    private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
        this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
}
