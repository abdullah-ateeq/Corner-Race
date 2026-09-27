package com.example.cornerrace.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.cornerrace.data.BoardTheme
import com.example.cornerrace.data.ThemeMode
import com.example.cornerrace.game.Player

private val DarkColors = darkColorScheme(
    primary = Bronze,
    onPrimary = Graphite,
    primaryContainer = BronzeDeep,
    onPrimaryContainer = Ivory,
    secondary = TealLight,
    tertiary = CopperLight,
    background = Graphite,
    onBackground = Ivory,
    surface = GraphiteRaised,
    onSurface = Ivory,
    surfaceVariant = GraphiteHigh,
    onSurfaceVariant = IvoryMuted,
    surfaceContainerHigh = GraphiteHigh,
    outline = GraphiteOutline,
    outlineVariant = Color(0xFF2C3036)
)

private val LightColors = lightColorScheme(
    primary = BronzeDeep,
    onPrimary = ParchmentRaised,
    primaryContainer = BronzeLight,
    onPrimaryContainer = Ink,
    secondary = Teal,
    tertiary = Copper,
    background = Parchment,
    onBackground = Ink,
    surface = ParchmentRaised,
    onSurface = Ink,
    surfaceVariant = ParchmentHigh,
    onSurfaceVariant = InkMuted,
    surfaceContainerHigh = ParchmentHigh,
    outline = ParchmentOutline,
    outlineVariant = Color(0xFFDDD5C8)
)

@Immutable
data class PieceColors(val base: Color, val light: Color, val deep: Color)

@Immutable
data class BoardColors(
    val frame: Color,
    val frameEdge: Color,
    val cellLight: Color,
    val cellDark: Color,
    val gridLine: Color,
    val hint: Color,
    val selectionRing: Color
)

@Immutable
data class CornerRaceColors(
    val player1: PieceColors,
    val player2: PieceColors,
    val board: BoardColors,
    val accent: Color
) {
    fun forPlayer(player: Player): PieceColors = if (player == Player.PLAYER_ONE) player1 else player2
}

val ClassicBoard = BoardColors(
    frame = Color(0xFF2A2621),
    frameEdge = Bronze,
    cellLight = Color(0xFFCFC3AE),
    cellDark = Color(0xFFC2B59E),
    gridLine = Color(0x22000000),
    hint = Color(0xFF3B3129),
    selectionRing = Color(0xFF2A211A)
)

val MidnightBoard = BoardColors(
    frame = Color(0xFF0B0D10),
    frameEdge = BronzeDeep,
    cellLight = Color(0xFF2C323B),
    cellDark = Color(0xFF262B33),
    gridLine = Color(0x26FFFFFF),
    hint = BronzeLight,
    selectionRing = BronzeLight
)

val LocalCornerRaceColors = staticCompositionLocalOf {
    CornerRaceColors(
        player1 = PieceColors(Copper, CopperLight, CopperDeep),
        player2 = PieceColors(Teal, TealLight, TealDeep),
        board = ClassicBoard,
        accent = Bronze
    )
}

object CornerRaceTheme {
    val colors: CornerRaceColors
        @Composable get() = LocalCornerRaceColors.current
}

@Composable
fun CornerRaceTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    boardTheme: BoardTheme = BoardTheme.CLASSIC,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = CornerRaceColors(
        player1 = PieceColors(Copper, CopperLight, CopperDeep),
        player2 = PieceColors(Teal, TealLight, TealDeep),
        board = if (boardTheme == BoardTheme.CLASSIC) ClassicBoard else MidnightBoard,
        accent = if (dark) Bronze else BronzeDeep
    )
    CompositionLocalProvider(LocalCornerRaceColors provides colors) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = Typography,
            content = content
        )
    }
}
