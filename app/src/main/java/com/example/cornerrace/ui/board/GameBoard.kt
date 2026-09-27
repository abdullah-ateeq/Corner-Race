package com.example.cornerrace.ui.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.cornerrace.game.BoardRules
import com.example.cornerrace.game.CornerRaceState
import com.example.cornerrace.game.GridPosition
import com.example.cornerrace.game.MoveType
import com.example.cornerrace.game.Player
import com.example.cornerrace.ui.game.PendingMove
import com.example.cornerrace.ui.theme.CornerRaceTheme
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun GameBoard(
    state: CornerRaceState,
    pendingMove: PendingMove?,
    showLegalMoves: Boolean,
    showTargetZones: Boolean,
    onCellTap: (GridPosition) -> Unit,
    onMoveAnimationFinished: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CornerRaceTheme.colors
    val board = colors.board
    val progress = remember { Animatable(0f) }
    val lift = remember(state.selectedPieceId) { Animatable(0f) }
    val finished = rememberUpdatedState(onMoveAnimationFinished)

    LaunchedEffect(pendingMove?.id) {
        val move = pendingMove ?: return@LaunchedEffect
        progress.snapTo(0f)
        progress.animateTo(1f, tween(move.durationMillis, easing = FastOutSlowInEasing))
        finished.value(move.id)
    }
    LaunchedEffect(state.selectedPieceId) {
        if (state.selectedPieceId != null) lift.animateTo(1f, tween(140))
    }

    val frameShape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(18.dp, frameShape, clip = false)
            .clip(frameShape)
            .background(board.frame)
            .border(1.dp, board.frameEdge.copy(alpha = 0.55f), frameShape)
            .padding(8.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val n = BoardRules.BOARD_SIZE
                val cell = size.minDimension / n
                drawCells(n, cell, board)
                if (showTargetZones) {
                    drawZone(BoardRules.player1StartZone, cell, colors.player1.base, 0.22f, 0.55f)
                    drawZone(BoardRules.player2StartZone, cell, colors.player2.base, 0.22f, 0.55f)
                }
                if (showLegalMoves && pendingMove == null) {
                    state.legalStepMoves.forEach { drawStepHint(cellCenter(it, cell), cell, board.hint) }
                    state.legalJumpMoves.forEach { drawJumpHint(cellCenter(it, cell), cell, board.hint) }
                }
                // Draw the active piece last so it passes over others while jumping.
                val ordered = state.pieces.sortedBy { if (it.id == state.selectedPieceId) 1 else 0 }
                for (piece in ordered) {
                    val moving = pendingMove?.takeIf { it.pieceId == piece.id }
                    val t = progress.value
                    val center = if (moving != null) {
                        lerp(cellCenter(moving.from, cell), cellCenter(moving.to, cell), t)
                    } else {
                        cellCenter(piece.position, cell)
                    }
                    val arc = if (moving?.type == MoveType.JUMP) sin(PI * t).toFloat() else 0f
                    val selected = piece.id == state.selectedPieceId
                    drawToken(
                        player = piece.player,
                        center = center,
                        cell = cell,
                        colors = colors.forPlayer(piece.player),
                        lift = if (selected) lift.value else 0f,
                        arc = arc,
                        selected = selected,
                        ringColor = board.selectionRing
                    )
                }
            }
            CellOverlay(state, onCellTap)
        }
    }
}

@Composable
private fun CellOverlay(
    state: CornerRaceState,
    onCellTap: (GridPosition) -> Unit
) {
    val n = BoardRules.BOARD_SIZE
    Column(Modifier.fillMaxSize()) {
        for (row in 0 until n) {
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (col in 0 until n) {
                    val position = GridPosition(row, col)
                    val description = describeCell(state, position)
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics {
                                contentDescription = description
                                role = Role.Button
                            }
                            .clickable(interactionSource = null, indication = null) { onCellTap(position) }
                    )
                }
            }
        }
    }
}

private fun playerLabel(player: Player) = "Player ${player.number}"

private fun describeCell(state: CornerRaceState, position: GridPosition): String {
    val where = "row ${position.row + 1} column ${position.col + 1}"
    val zone = when (position) {
        in BoardRules.player2StartZone -> ", ${playerLabel(Player.PLAYER_ONE)} target zone"
        in BoardRules.player1StartZone -> ", ${playerLabel(Player.PLAYER_TWO)} target zone"
        else -> ""
    }
    val piece = state.pieceAt(position)
    return when {
        piece != null -> buildString {
            append("${playerLabel(piece.player)} piece, $where")
            if (piece.id == state.selectedPieceId) append(", selected")
            append(zone)
        }
        position in state.legalJumpMoves -> "Legal jump destination, $where$zone"
        position in state.legalStepMoves -> "Legal move destination, $where$zone"
        else -> "Empty cell, $where$zone"
    }
}
