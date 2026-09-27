package com.example.cornerrace.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cornerrace.data.GameStats
import com.example.cornerrace.game.Player
import com.example.cornerrace.ui.components.OrnamentDivider
import com.example.cornerrace.ui.components.PlayerToken
import com.example.cornerrace.ui.components.PrimaryButton
import com.example.cornerrace.ui.components.SecondaryButton
import com.example.cornerrace.ui.components.StatTile

@Composable
fun HomeScreen(
    stats: GameStats,
    onPlay: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerToken(Player.PLAYER_ONE, 34.dp)
                Spacer(Modifier.width(18.dp))
                PlayerToken(Player.PLAYER_TWO, 34.dp)
            }
            Spacer(Modifier.height(28.dp))
            Text(
                "CORNER RACE",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(14.dp))
            OrnamentDivider()
            Spacer(Modifier.height(14.dp))
            Text(
                "Move smart. Jump ahead.\nReach the opposite corner.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(44.dp))
            PrimaryButton("PLAY", onPlay, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            SecondaryButton("HOW TO PLAY", onHowToPlay, Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = onSettings) {
                    Text("SETTINGS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onAbout) {
                    Text("ABOUT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Games", stats.gamesPlayed.toString(), Modifier.weight(1f))
                StatTile("P1 Wins", stats.player1Wins.toString(), Modifier.weight(1f))
                StatTile("P2 Wins", stats.player2Wins.toString(), Modifier.weight(1f))
            }
        }
    }
}
