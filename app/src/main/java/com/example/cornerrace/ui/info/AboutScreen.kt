package com.example.cornerrace.ui.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cornerrace.game.Player
import com.example.cornerrace.ui.components.OrnamentDivider
import com.example.cornerrace.ui.components.PlayerToken
import com.example.cornerrace.ui.components.SectionCard
import com.example.cornerrace.ui.components.SectionLabel
import com.example.cornerrace.ui.components.SubScreen
import com.example.cornerrace.ui.theme.CornerRaceTheme

private val features = listOf(
    "Local 2 Player",
    "8×8 Board",
    "Step & Multi-Jump Movement",
    "Opposite-Corner Objective",
    "Offline Play"
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    SubScreen(title = "About", onBack = onBack) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerToken(Player.PLAYER_ONE, 30.dp)
                Spacer(Modifier.width(14.dp))
                PlayerToken(Player.PLAYER_TWO, 30.dp)
            }
            Spacer(Modifier.height(20.dp))
            Text("Corner Race", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text("Version 1.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            OrnamentDivider()
            Spacer(Modifier.height(16.dp))
            Text(
                "A polished local two-player strategy race where players step and jump across the board " +
                    "to move every piece into the opposite corner.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        SectionLabel("Features")
        SectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("◆", color = CornerRaceTheme.colors.accent, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(12.dp))
                        Text(feature, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
