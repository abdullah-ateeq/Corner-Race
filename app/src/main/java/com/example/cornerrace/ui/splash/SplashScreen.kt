package com.example.cornerrace.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.cornerrace.game.BoardRules
import com.example.cornerrace.game.GridPosition
import com.example.cornerrace.game.Player
import com.example.cornerrace.ui.board.cellCenter
import com.example.cornerrace.ui.board.drawCells
import com.example.cornerrace.ui.board.drawToken
import com.example.cornerrace.ui.board.drawZone
import com.example.cornerrace.ui.components.OrnamentDivider
import com.example.cornerrace.ui.theme.CornerRaceTheme
import kotlin.math.PI
import kotlin.math.sin

private const val SPLASH_MS = 1500

/** Maps overall time [t] (ms) into 0..1 progress for a phase running from [start] for [length] ms. */
private fun phase(t: Float, start: Float, length: Float): Float =
    FastOutSlowInEasing.transform(((t - start) / length).coerceIn(0f, 1f))

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val time = remember { Animatable(0f) }
    val done by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        time.animateTo(SPLASH_MS.toFloat(), tween(SPLASH_MS, easing = LinearEasing))
        done()
    }
    val colors = CornerRaceTheme.colors
    val t = time.value
    val boardIn = phase(t, 0f, 350f)
    val piecesIn = phase(t, 200f, 350f)
    val jump = phase(t, 550f, 320f)
    val targetGlow = phase(t, 800f, 300f)
    val titleIn = phase(t, 950f, 350f)

    val jumpFrom = GridPosition(4, 0)
    val jumpTo = GridPosition(2, 2)

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Canvas(
                Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        alpha = boardIn
                        scaleX = 0.94f + 0.06f * boardIn
                        scaleY = 0.94f + 0.06f * boardIn
                    }
            ) {
                val n = BoardRules.BOARD_SIZE
                val pad = size.minDimension * 0.04f
                val inner = size.minDimension - pad * 2
                val cell = inner / n
                drawRoundRect(colors.board.frame, cornerRadius = CornerRadius(pad * 2))
                translate(pad, pad) {
                    clipRect(0f, 0f, inner, inner) {
                        drawCells(n, cell, colors.board)
                        drawZone(BoardRules.player1StartZone, cell, colors.player1.base, 0.2f, 0.4f)
                        drawZone(
                            BoardRules.player2StartZone, cell, colors.player2.base,
                            0.2f + 0.25f * targetGlow * (1f - 0.4f * titleIn), 0.4f + 0.5f * targetGlow
                        )
                        // A middle blocker the demo piece jumps over.
                        val blocker = GridPosition(3, 1)
                        val tokens = BoardRules.player1StartZone.filter { it != jumpFrom }.map { Player.PLAYER_ONE to it } +
                            BoardRules.player2StartZone.map { Player.PLAYER_TWO to it } +
                            listOf(Player.PLAYER_TWO to blocker)
                        tokens.forEachIndexed { index, (player, p) ->
                            val appear = ((piecesIn * 1.6f) - index * 0.03f).coerceIn(0f, 1f)
                            drawToken(player, cellCenter(p, cell), cell, colors.forPlayer(player), alpha = appear)
                        }
                        val center = lerp(cellCenter(jumpFrom, cell), cellCenter(jumpTo, cell), jump)
                        drawToken(
                            Player.PLAYER_ONE, center, cell, colors.player1,
                            arc = sin(PI * jump).toFloat(), alpha = piecesIn
                        )
                    }
                }
                drawRoundRect(
                    colors.board.frameEdge.copy(alpha = 0.5f),
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(pad * 2),
                    style = Stroke(1.dp.toPx())
                )
            }
            Spacer(Modifier.height(36.dp))
            Column(
                Modifier
                    .alpha(titleIn)
                    .graphicsLayer { translationY = (1f - titleIn) * 16.dp.toPx() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("CORNER RACE", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(14.dp))
                OrnamentDivider()
            }
        }
    }
}
