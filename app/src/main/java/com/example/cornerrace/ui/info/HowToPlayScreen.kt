package com.example.cornerrace.ui.info

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.cornerrace.game.GridPosition
import com.example.cornerrace.game.Player
import com.example.cornerrace.ui.board.cellCenter
import com.example.cornerrace.ui.board.drawCells
import com.example.cornerrace.ui.board.drawJumpHint
import com.example.cornerrace.ui.board.drawStepHint
import com.example.cornerrace.ui.board.drawToken
import com.example.cornerrace.ui.components.PlayerToken
import com.example.cornerrace.ui.components.SectionCard
import com.example.cornerrace.ui.components.SectionLabel
import com.example.cornerrace.ui.components.SubScreen
import com.example.cornerrace.ui.theme.CornerRaceTheme

private val rules = listOf(
    "Move one piece per turn.",
    "Step into any adjacent empty cell — up, down, sideways or diagonal.",
    "Jump over any adjacent piece, yours or your rival's, into the empty cell directly beyond.",
    "After a jump, keep jumping with the same piece if you can, or tap END TURN to stop.",
    "Pieces are never captured. Jumped pieces stay where they are.",
    "Move all 8 pieces into the opposite corner to win."
)

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    SubScreen(title = "How to Play", onBack = onBack) {
        SectionLabel("The Race")
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerToken(Player.PLAYER_ONE, 26.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Player 1 starts bottom-left and races to the top-right corner.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerToken(Player.PLAYER_TWO, 26.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Player 2 starts top-right and races to the bottom-left corner.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        SectionLabel("Rules")
        SectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rules.forEachIndexed { index, rule ->
                    Row {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            color = CornerRaceTheme.colors.accent,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(rule, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        SectionLabel("Example")
        SectionCard {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ExampleBoard()
            }
            Spacer(Modifier.height(14.dp))
            LegendRow(isJump = false, text = "Dot — step into an adjacent empty cell")
            Spacer(Modifier.height(8.dp))
            LegendRow(isJump = true, text = "Ring — jump over a piece into the empty cell beyond")
        }
    }
}

@Composable
private fun ExampleBoard() {
    val colors = CornerRaceTheme.colors
    val n = 5
    val selected = GridPosition(2, 2)
    val others = listOf(Player.PLAYER_TWO to GridPosition(2, 3), Player.PLAYER_ONE to GridPosition(1, 1))
    val occupied = others.map { it.second }.toSet() + selected
    val jumps = setOf(GridPosition(2, 4), GridPosition(0, 0))
    Canvas(
        Modifier
            .size(220.dp)
            .clip(RoundedCornerShape(10.dp))
            .semantics {
                contentDescription = "Example: a selected Player 1 piece can step to any adjacent empty cell, " +
                    "or jump over the neighbouring Player 2 piece or its own piece."
            }
    ) {
        val cell = size.minDimension / n
        drawCells(n, cell, colors.board)
        for (r in 1..3) for (c in 1..3) {
            val p = GridPosition(r, c)
            if (p !in occupied) drawStepHint(cellCenter(p, cell), cell, colors.board.hint)
        }
        jumps.forEach { drawJumpHint(cellCenter(it, cell), cell, colors.board.hint) }
        others.forEach { (player, p) -> drawToken(player, cellCenter(p, cell), cell, colors.forPlayer(player)) }
        drawToken(
            Player.PLAYER_ONE, cellCenter(selected, cell), cell, colors.player1,
            lift = 1f, selected = true, ringColor = colors.board.selectionRing
        )
    }
}

@Composable
private fun LegendRow(isJump: Boolean, text: String) {
    val hint = CornerRaceTheme.colors.board.hint
    val cellColor = CornerRaceTheme.colors.board.cellLight
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            drawRect(cellColor)
            val cell = size.minDimension
            if (isJump) drawJumpHint(center, cell, hint) else drawStepHint(center, cell * 1.4f, hint)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 4.dp))
    }
}
