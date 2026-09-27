package com.example.cornerrace.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.cornerrace.data.AppSettings
import com.example.cornerrace.data.BoardTheme
import com.example.cornerrace.data.GameStats
import com.example.cornerrace.data.ThemeMode
import com.example.cornerrace.ui.components.SecondaryButton
import com.example.cornerrace.ui.components.SectionCard
import com.example.cornerrace.ui.components.SectionLabel
import com.example.cornerrace.ui.components.StatTile
import com.example.cornerrace.ui.components.SubScreen
import com.example.cornerrace.ui.game.ConfirmDialog

@Composable
fun SettingsScreen(
    settings: AppSettings,
    stats: GameStats,
    onBack: () -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onBoardTheme: (BoardTheme) -> Unit,
    onSound: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onShowLegalMoves: (Boolean) -> Unit,
    onShowTargetZones: (Boolean) -> Unit,
    onResetStats: () -> Unit
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    SubScreen(title = "Settings", onBack = onBack) {
        SectionLabel("Appearance")
        SectionCard {
            Text("Theme", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(10.dp))
            Segmented(ThemeMode.entries, settings.themeMode, { it.label }, onThemeMode)
            Spacer(Modifier.height(18.dp))
            Text("Board Theme", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(10.dp))
            Segmented(BoardTheme.entries, settings.boardTheme, { it.label }, onBoardTheme)
        }

        SectionLabel("Gameplay")
        SectionCard {
            ToggleRow("Show Legal Moves", settings.showLegalMoves, onShowLegalMoves)
            RowDivider()
            ToggleRow("Show Target Zones", settings.showTargetZones, onShowTargetZones)
        }

        SectionLabel("Feedback")
        SectionCard {
            ToggleRow("Sound Effects", settings.soundEnabled, onSound)
            RowDivider()
            ToggleRow("Haptic Feedback", settings.hapticsEnabled, onHaptics)
        }

        SectionLabel("Statistics")
        SectionCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("Games", stats.gamesPlayed.toString(), Modifier.weight(1f))
                StatTile("P1 Wins", stats.player1Wins.toString(), Modifier.weight(1f))
                StatTile("P2 Wins", stats.player2Wins.toString(), Modifier.weight(1f))
                StatTile("Best", stats.fewestTurnsToWin?.toString() ?: "—", Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Best = fewest turns to win",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            SecondaryButton(
                "RESET STATISTICS",
                { confirmReset = true },
                Modifier.fillMaxWidth(),
                enabled = stats.gamesPlayed > 0
            )
        }
    }

    if (confirmReset) {
        ConfirmDialog(
            title = "Reset statistics?",
            message = "Games played, wins and best result will be cleared.",
            confirm = "RESET",
            onConfirm = {
                confirmReset = false
                onResetStats()
            },
            onDismiss = { confirmReset = false }
        )
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

@Composable
private fun <T> Segmented(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 42.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label(option).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
