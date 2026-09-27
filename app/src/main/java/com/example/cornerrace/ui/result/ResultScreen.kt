package com.example.cornerrace.ui.result

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cornerrace.game.BoardRules
import com.example.cornerrace.ui.components.OrnamentDivider
import com.example.cornerrace.ui.components.PlayerToken
import com.example.cornerrace.ui.components.PrimaryButton
import com.example.cornerrace.ui.components.SecondaryButton
import com.example.cornerrace.ui.components.StatTile
import com.example.cornerrace.ui.game.MatchResult
import com.example.cornerrace.ui.theme.CornerRaceTheme

@Composable
fun ResultScreen(result: MatchResult, onPlayAgain: () -> Unit, onHome: () -> Unit) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(600)) }
    val winnerColor = CornerRaceTheme.colors.forPlayer(result.winner).base
    val total = BoardRules.PIECES_PER_PLAYER

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp)
                .graphicsLayer {
                    alpha = reveal.value
                    translationY = (1f - reveal.value) * 24.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlayerToken(result.winner, 64.dp)
            Spacer(Modifier.height(24.dp))
            Text(
                "VICTORY",
                style = MaterialTheme.typography.labelMedium,
                color = winnerColor
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "PLAYER ${result.winner.number} WINS",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(14.dp))
            OrnamentDivider()
            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Turns Played", result.turns.toString(), Modifier.weight(1f))
                StatTile("Total Jumps", result.totalJumps.toString(), Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    "Pieces in Target",
                    "${result.player1InTarget}/$total · ${result.player2InTarget}/$total",
                    Modifier.weight(1f)
                )
                StatTile("Match Duration", formatDuration(result.durationMillis), Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Pieces in target shown as Player 1 · Player 2",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(40.dp))
            PrimaryButton("PLAY AGAIN", onPlayAgain, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            SecondaryButton("HOME", onHome, Modifier.fillMaxWidth())
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    return "%d:%02d".format(seconds / 60, seconds % 60)
}
