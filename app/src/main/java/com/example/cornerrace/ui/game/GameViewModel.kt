package com.example.cornerrace.ui.game

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cornerrace.data.PreferencesRepository
import com.example.cornerrace.feedback.FeedbackEvent
import com.example.cornerrace.game.CornerRaceGame
import com.example.cornerrace.game.CornerRaceState
import com.example.cornerrace.game.GridPosition
import com.example.cornerrace.game.MoveType
import com.example.cornerrace.game.Player
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PendingMove(
    val id: Long,
    val pieceId: Int,
    val from: GridPosition,
    val to: GridPosition,
    val type: MoveType
) {
    val durationMillis: Int get() = if (type == MoveType.JUMP) 260 else 180
}

data class MatchResult(
    val winner: Player,
    val turns: Int,
    val totalJumps: Int,
    val player1InTarget: Int,
    val player2InTarget: Int,
    val durationMillis: Long
)

data class GameUiState(
    val game: CornerRaceState,
    val pendingMove: PendingMove? = null,
    val canUndo: Boolean = false,
    val result: MatchResult? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository(application)
    private val engine = CornerRaceGame()
    private var startedAt = SystemClock.elapsedRealtime()
    private var nextMoveId = 1L

    private val _uiState = MutableStateFlow(GameUiState(engine.state))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FeedbackEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<FeedbackEvent> = _events

    fun onCellTapped(position: GridPosition) {
        val ui = _uiState.value
        val s = engine.state
        if (ui.pendingMove != null || s.isAnimating || s.winner != null) return

        if (position in s.legalJumpMoves || position in s.legalStepMoves) {
            startMove(position)
            return
        }
        val piece = s.pieceAt(position)
        when {
            s.isJumpChain -> Unit
            piece != null && piece.player == s.currentPlayer -> {
                if (s.selectedPieceId == piece.id) {
                    engine.deselect()
                } else if (engine.selectPiece(piece.id)) {
                    _events.tryEmit(FeedbackEvent.SELECT)
                }
            }
            else -> engine.deselect()
        }
        publish()
    }

    private fun startMove(destination: GridPosition) {
        val s = engine.state
        val piece = s.selectedPiece ?: return
        val type = if (destination in s.legalJumpMoves) MoveType.JUMP else MoveType.STEP
        engine.setAnimating(true)
        _uiState.update {
            it.copy(pendingMove = PendingMove(nextMoveId++, piece.id, piece.position, destination, type))
        }
        publish()
    }

    /** Called by the board once the travel animation for [moveId] has finished. */
    fun onMoveAnimationFinished(moveId: Long) {
        val pending = _uiState.value.pendingMove ?: return
        if (pending.id != moveId) return
        engine.setAnimating(false)
        val outcome = engine.moveSelectedPiece(pending.to)
        _uiState.update { it.copy(pendingMove = null) }
        if (outcome != null) {
            _events.tryEmit(if (outcome.type == MoveType.JUMP) FeedbackEvent.JUMP else FeedbackEvent.STEP)
            if (outcome.enteredTarget && outcome.winner == null) _events.tryEmit(FeedbackEvent.TARGET_ENTRY)
            outcome.winner?.let { onWin(it) }
        }
        publish()
    }

    fun endJumpTurn() {
        if (_uiState.value.pendingMove != null) return
        if (engine.endJumpTurn()) publish()
    }

    fun undo() {
        if (_uiState.value.pendingMove != null) return
        if (engine.undo()) publish()
    }

    fun restart() {
        engine.restart()
        startedAt = SystemClock.elapsedRealtime()
        _uiState.value = GameUiState(engine.state)
    }

    private fun onWin(winner: Player) {
        val s = engine.state
        val result = MatchResult(
            winner = winner,
            turns = s.turns,
            totalJumps = s.totalJumps,
            player1InTarget = s.piecesInTarget(Player.PLAYER_ONE),
            player2InTarget = s.piecesInTarget(Player.PLAYER_TWO),
            durationMillis = SystemClock.elapsedRealtime() - startedAt
        )
        _uiState.update { it.copy(result = result) }
        _events.tryEmit(FeedbackEvent.WIN)
        viewModelScope.launch { repository.recordWin(winner, s.turns) }
    }

    private fun publish() {
        _uiState.update { it.copy(game = engine.state, canUndo = engine.canUndo) }
    }
}
