package com.example.cornerrace.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cornerrace.data.AppSettings
import com.example.cornerrace.feedback.Haptics
import com.example.cornerrace.feedback.LocalSoundEngine
import com.example.cornerrace.feedback.toSfx
import com.example.cornerrace.game.BoardRules
import com.example.cornerrace.game.Player
import com.example.cornerrace.ui.board.GameBoard
import com.example.cornerrace.ui.components.PlayerToken
import com.example.cornerrace.ui.components.PrimaryButton
import com.example.cornerrace.ui.components.SecondaryButton
import com.example.cornerrace.ui.theme.CornerRaceTheme
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    settings: AppSettings,
    onExit: () -> Unit,
    onFinished: (MatchResult) -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val sound = LocalSoundEngine.current
    val currentSettings by rememberUpdatedState(settings)
    val finish by rememberUpdatedState(onFinished)

    var confirmRestart by rememberSaveable { mutableStateOf(false) }
    var confirmLeave by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (currentSettings.soundEnabled) sound?.play(event.toSfx())
            if (currentSettings.hapticsEnabled) Haptics.perform(view, event)
        }
    }
    LaunchedEffect(ui.result) {
        val result = ui.result ?: return@LaunchedEffect
        delay(1100)
        finish(result)
    }

    val requestLeave = {
        if (ui.game.turns > 0 && ui.result == null) confirmLeave = true else onExit()
    }
    BackHandler(onBack = requestLeave)

    val game = ui.game
    val board: @Composable (Modifier) -> Unit = { modifier ->
        GameBoard(
            state = game,
            pendingMove = ui.pendingMove,
            showLegalMoves = settings.showLegalMoves,
            showTargetZones = settings.showTargetZones,
            onCellTap = viewModel::onCellTapped,
            onMoveAnimationFinished = viewModel::onMoveAnimationFinished,
            modifier = modifier
        )
    }
    val turnBar: @Composable () -> Unit = {
        TurnBar(
            player = game.currentPlayer,
            winner = game.winner,
            isJumpChain = game.isJumpChain,
            endTurnEnabled = game.isJumpChain && ui.pendingMove == null,
            onEndTurn = viewModel::endJumpTurn
        )
    }
    val controls: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("UNDO", viewModel::undo, Modifier.weight(1f), enabled = ui.canUndo)
            SecondaryButton("RESTART", { confirmRestart = true }, Modifier.weight(1f), enabled = ui.result == null)
            SecondaryButton("MENU", requestLeave, Modifier.weight(1f))
        }
    }
    val panel: @Composable (Player) -> Unit = { player ->
        PlayerPanel(
            player = player,
            inTarget = game.piecesInTarget(player),
            isActive = game.winner == null && game.currentPlayer == player,
            isWinner = game.winner == player
        )
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val landscape = maxWidth > maxHeight * 1.1f
        val sidePanelWidth = if (maxWidth > 900.dp) 380.dp else 300.dp
        if (landscape) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) { board(Modifier) }
                Column(
                    Modifier
                        .width(sidePanelWidth)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    GameHeader(turns = game.turns)
                    panel(Player.PLAYER_TWO)
                    panel(Player.PLAYER_ONE)
                    turnBar()
                    controls()
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
            ) {
                GameHeader(turns = game.turns)
                panel(Player.PLAYER_TWO)
                Box(
                    Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { board(Modifier) }
                panel(Player.PLAYER_ONE)
                turnBar()
                controls()
            }
        }
    }

    if (confirmRestart) {
        ConfirmDialog(
            title = "Restart this race?",
            message = "The current board will be cleared.",
            confirm = "RESTART",
            onConfirm = {
                confirmRestart = false
                viewModel.restart()
            },
            onDismiss = { confirmRestart = false }
        )
    }
    if (confirmLeave) {
        ConfirmDialog(
            title = "Leave this race?",
            message = "The current game will not be saved.",
            confirm = "LEAVE",
            onConfirm = {
                confirmLeave = false
                onExit()
            },
            onDismiss = { confirmLeave = false }
        )
    }
}

@Composable
private fun GameHeader(turns: Int) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "CORNER RACE",
            style = MaterialTheme.typography.titleSmall,
            color = CornerRaceTheme.colors.accent,
            modifier = Modifier.weight(1f)
        )
        Text(
            "TURN ${turns + 1}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlayerPanel(player: Player, inTarget: Int, isActive: Boolean, isWinner: Boolean) {
    val playerColors = CornerRaceTheme.colors.forPlayer(player)
    val borderColor by animateColorAsState(
        if (isActive || isWinner) playerColors.base else MaterialTheme.colorScheme.outlineVariant,
        label = "panelBorder"
    )
    val total = BoardRules.PIECES_PER_PLAYER
    val description = buildString {
        append("Player ${player.number} has $inTarget of $total pieces in target")
        if (isActive) append(", current turn")
        if (isWinner) append(", winner")
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isActive || isWinner) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerToken(player, 30.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "PLAYER ${player.number}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Target: $inTarget / $total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ProgressPips(filled = inTarget, total = total, color = playerColors.base)
        }
    }
}

@Composable
private fun ProgressPips(filled: Int, total: Int, color: Color) {
    val empty = MaterialTheme.colorScheme.outlineVariant
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(total) { i ->
            val c by animateColorAsState(if (i < filled) color else empty, label = "pip")
            Box(
                Modifier
                    .size(width = 8.dp, height = 18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(c)
            )
        }
    }
}

@Composable
private fun TurnBar(
    player: Player,
    winner: Player?,
    isJumpChain: Boolean,
    endTurnEnabled: Boolean,
    onEndTurn: () -> Unit
) {
    val shown = winner ?: player
    val label = when {
        winner != null -> "PLAYER ${winner.number} WINS"
        isJumpChain -> "PLAYER ${player.number} · KEEP JUMPING"
        else -> "PLAYER ${player.number}'S TURN"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerToken(shown, 20.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        AnimatedVisibility(
            visible = isJumpChain && winner == null,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            PrimaryButton("END TURN", onEndTurn, enabled = endTurnEnabled)
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirm: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirm, style = MaterialTheme.typography.labelLarge, color = CornerRaceTheme.colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
