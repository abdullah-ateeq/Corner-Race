package com.example.cornerrace.game

enum class Player(val number: Int) {
    PLAYER_ONE(1),
    PLAYER_TWO(2);

    val opponent: Player
        get() = if (this == PLAYER_ONE) PLAYER_TWO else PLAYER_ONE
}

data class GridPosition(
    val row: Int,
    val col: Int
) {
    operator fun plus(direction: Direction): GridPosition =
        GridPosition(row + direction.dRow, col + direction.dCol)
}

data class Direction(val dRow: Int, val dCol: Int) {
    operator fun times(factor: Int): Direction = Direction(dRow * factor, dCol * factor)
}

data class CornerPiece(
    val id: Int,
    val player: Player,
    val position: GridPosition
)

data class CornerRaceState(
    val pieces: List<CornerPiece>,
    val currentPlayer: Player = Player.PLAYER_ONE,
    val selectedPieceId: Int? = null,
    val legalStepMoves: Set<GridPosition> = emptySet(),
    val legalJumpMoves: Set<GridPosition> = emptySet(),
    val isJumpChain: Boolean = false,
    val visitedJumpPositions: Set<GridPosition> = emptySet(),
    val winner: Player? = null,
    val turns: Int = 0,
    val totalJumps: Int = 0,
    val isAnimating: Boolean = false
) {
    val selectedPiece: CornerPiece?
        get() = selectedPieceId?.let { id -> pieces.firstOrNull { it.id == id } }

    fun pieceAt(position: GridPosition): CornerPiece? = pieces.firstOrNull { it.position == position }

    fun piecesInTarget(player: Player): Int = BoardRules.piecesInTarget(player, pieces)

    companion object {
        fun initial(): CornerRaceState = CornerRaceState(pieces = BoardRules.initialPieces())
    }
}

enum class MoveType { STEP, JUMP }

data class MoveOutcome(
    val pieceId: Int,
    val from: GridPosition,
    val to: GridPosition,
    val type: MoveType,
    val enteredTarget: Boolean,
    val chainContinues: Boolean,
    val winner: Player?
)
