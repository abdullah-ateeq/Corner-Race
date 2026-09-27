package com.example.cornerrace.game

/**
 * Turn engine for a local match. Holds the current [state] and a snapshot history for undo.
 * Contains no Android dependencies so it can be unit tested directly.
 */
class CornerRaceGame(initialState: CornerRaceState = CornerRaceState.initial()) {

    var state: CornerRaceState = initialState
        private set

    private val history = ArrayDeque<CornerRaceState>()

    val canUndo: Boolean
        get() = history.isNotEmpty() && !state.isJumpChain && !state.isAnimating && state.winner == null

    fun restart() {
        history.clear()
        state = CornerRaceState.initial()
    }

    fun setAnimating(animating: Boolean) {
        state = state.copy(isAnimating = animating)
    }

    fun hasWon(player: Player): Boolean = BoardRules.hasWon(player, state.pieces)

    fun selectPiece(pieceId: Int): Boolean {
        val s = state
        if (s.winner != null || s.isAnimating || s.isJumpChain) return false
        val piece = s.pieces.firstOrNull { it.id == pieceId } ?: return false
        if (piece.player != s.currentPlayer) return false
        state = s.copy(
            selectedPieceId = piece.id,
            legalStepMoves = BoardRules.getStepMoves(piece, s.pieces),
            legalJumpMoves = BoardRules.getJumpMoves(piece, s.pieces, setOf(piece.position))
        )
        return true
    }

    fun deselect(): Boolean {
        val s = state
        if (s.isJumpChain || s.isAnimating || s.selectedPieceId == null) return false
        state = s.clearSelection()
        return true
    }

    fun moveSelectedPiece(destination: GridPosition): MoveOutcome? {
        val s = state
        if (s.winner != null || s.isAnimating) return null
        val piece = s.selectedPiece ?: return null
        if (piece.player != s.currentPlayer) return null

        val type = when (destination) {
            in s.legalJumpMoves -> MoveType.JUMP
            in s.legalStepMoves -> MoveType.STEP
            else -> return null
        }
        // Re-validate against the live board rather than trusting cached hints.
        val valid = when (type) {
            MoveType.STEP -> destination in BoardRules.getStepMoves(piece, s.pieces)
            MoveType.JUMP -> destination in BoardRules.getJumpMoves(piece, s.pieces, s.visitedJumpPositions + piece.position)
        }
        if (!valid) return null

        if (!s.isJumpChain) history.addLast(s.clearSelection())

        val target = BoardRules.targetZone(piece.player)
        val moved = piece.copy(position = destination)
        val pieces = s.pieces.map { if (it.id == piece.id) moved else it }
        val won = BoardRules.hasWon(piece.player, pieces)
        val enteredTarget = piece.position !in target && destination in target

        state = when (type) {
            MoveType.STEP -> finishTurn(s.copy(pieces = pieces), won)
            MoveType.JUMP -> {
                val visited = s.visitedJumpPositions + piece.position + destination
                val jumped = s.copy(pieces = pieces, totalJumps = s.totalJumps + 1)
                val further = BoardRules.getJumpMoves(moved, pieces, visited)
                if (won || further.isEmpty()) {
                    finishTurn(jumped, won)
                } else {
                    jumped.copy(
                        selectedPieceId = piece.id,
                        legalStepMoves = emptySet(),
                        legalJumpMoves = further,
                        isJumpChain = true,
                        visitedJumpPositions = visited
                    )
                }
            }
        }

        return MoveOutcome(
            pieceId = piece.id,
            from = piece.position,
            to = destination,
            type = type,
            enteredTarget = enteredTarget,
            chainContinues = state.isJumpChain,
            winner = state.winner
        )
    }

    fun endJumpTurn(): Boolean {
        val s = state
        if (!s.isJumpChain || s.isAnimating || s.winner != null) return false
        state = finishTurn(s, won = false)
        return true
    }

    fun undo(): Boolean {
        if (!canUndo) return false
        state = history.removeLast()
        return true
    }

    private fun finishTurn(s: CornerRaceState, won: Boolean): CornerRaceState {
        val ended = s.clearSelection().copy(turns = s.turns + 1)
        if (won) return ended.copy(winner = s.currentPlayer)
        val next = s.currentPlayer.opponent
        // A fully blocked player passes; practically unreachable but keeps the game from freezing.
        val player = if (BoardRules.hasAnyMove(next, s.pieces)) next else s.currentPlayer
        return ended.copy(currentPlayer = player)
    }

    private fun CornerRaceState.clearSelection(): CornerRaceState = copy(
        selectedPieceId = null,
        legalStepMoves = emptySet(),
        legalJumpMoves = emptySet(),
        isJumpChain = false,
        visitedJumpPositions = emptySet()
    )
}
