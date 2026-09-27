package com.example.cornerrace.game

/**
 * Fixed board geometry and pure move generation. Row 0 is the top of the board.
 */
object BoardRules {
    const val BOARD_SIZE = 8
    const val PIECES_PER_PLAYER = 8

    val directions: List<Direction> = listOf(
        Direction(-1, -1), Direction(-1, 0), Direction(-1, 1),
        Direction(0, -1), Direction(0, 1),
        Direction(1, -1), Direction(1, 0), Direction(1, 1)
    )

    /** Staggered triangle hugging the bottom-left corner (4 + 2 + 1 + 1 cells). */
    val player1StartZone: Set<GridPosition> = linkedSetOf(
        GridPosition(7, 0), GridPosition(7, 1), GridPosition(7, 2), GridPosition(7, 3),
        GridPosition(6, 0), GridPosition(6, 1),
        GridPosition(5, 0),
        GridPosition(4, 0)
    )

    /** Point mirror of [player1StartZone] in the top-right corner. */
    val player2StartZone: Set<GridPosition> =
        player1StartZone.mapTo(linkedSetOf()) { GridPosition(BOARD_SIZE - 1 - it.row, BOARD_SIZE - 1 - it.col) }

    fun startZone(player: Player): Set<GridPosition> =
        if (player == Player.PLAYER_ONE) player1StartZone else player2StartZone

    fun targetZone(player: Player): Set<GridPosition> = startZone(player.opponent)

    fun isInside(position: GridPosition): Boolean =
        position.row in 0 until BOARD_SIZE && position.col in 0 until BOARD_SIZE

    fun initialPieces(): List<CornerPiece> {
        val p1 = player1StartZone.mapIndexed { i, pos -> CornerPiece(i, Player.PLAYER_ONE, pos) }
        val p2 = player2StartZone.mapIndexed { i, pos -> CornerPiece(PIECES_PER_PLAYER + i, Player.PLAYER_TWO, pos) }
        return p1 + p2
    }

    fun getStepMoves(piece: CornerPiece, pieces: List<CornerPiece>): Set<GridPosition> {
        val occupied = pieces.mapTo(HashSet()) { it.position }
        return directions.mapNotNullTo(linkedSetOf()) { dir ->
            val destination = piece.position + dir
            destination.takeIf { isInside(it) && it !in occupied }
        }
    }

    fun getJumpMoves(
        piece: CornerPiece,
        pieces: List<CornerPiece>,
        visited: Set<GridPosition> = emptySet()
    ): Set<GridPosition> {
        val occupied = pieces.mapTo(HashSet()) { it.position }
        return directions.mapNotNullTo(linkedSetOf()) { dir ->
            val middle = piece.position + dir
            val destination = piece.position + dir * 2
            destination.takeIf {
                isInside(middle) && middle in occupied &&
                    isInside(destination) && destination !in occupied &&
                    destination !in visited
            }
        }
    }

    fun piecesInTarget(player: Player, pieces: List<CornerPiece>): Int {
        val target = targetZone(player)
        return pieces.count { it.player == player && it.position in target }
    }

    fun hasWon(player: Player, pieces: List<CornerPiece>): Boolean {
        val own = pieces.filter { it.player == player }
        val target = targetZone(player)
        return own.size == PIECES_PER_PLAYER && own.all { it.position in target }
    }

    fun hasAnyMove(player: Player, pieces: List<CornerPiece>): Boolean =
        pieces.any { it.player == player && (getStepMoves(it, pieces).isNotEmpty() || getJumpMoves(it, pieces).isNotEmpty()) }
}
