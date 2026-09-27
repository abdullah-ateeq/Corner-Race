package com.example.cornerrace.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CornerRaceGameTest {

    private val p1 = Player.PLAYER_ONE
    private val p2 = Player.PLAYER_TWO

    private fun pos(row: Int, col: Int) = GridPosition(row, col)

    private fun gameOf(vararg pieces: CornerPiece, current: Player = p1) =
        CornerRaceGame(CornerRaceState(pieces = pieces.toList(), currentPlayer = current))

    private fun piece(id: Int, player: Player, row: Int, col: Int) = CornerPiece(id, player, pos(row, col))

    @Test
    fun initialSetup() {
        val state = CornerRaceState.initial()
        assertEquals(16, state.pieces.size)
        assertEquals(8, BoardRules.player1StartZone.size)
        assertEquals(8, BoardRules.player2StartZone.size)
        assertTrue(BoardRules.player1StartZone.intersect(BoardRules.player2StartZone).isEmpty())
        assertEquals(p1, state.currentPlayer)
        assertTrue(state.pieces.filter { it.player == p1 }.all { it.position in BoardRules.player1StartZone })
        assertTrue(state.pieces.filter { it.player == p2 }.all { it.position in BoardRules.player2StartZone })
        assertEquals(BoardRules.player2StartZone, BoardRules.targetZone(p1))
        assertEquals(BoardRules.player1StartZone, BoardRules.targetZone(p2))
        assertEquals(state.pieces.size, state.pieces.map { it.position }.toSet().size)
    }

    @Test
    fun adjacentStepOffersAllEightDirections() {
        val game = gameOf(piece(0, p1, 4, 4), piece(8, p2, 0, 7))
        assertTrue(game.selectPiece(0))
        assertEquals(8, game.state.legalStepMoves.size)
        val outcome = game.moveSelectedPiece(pos(3, 5))
        assertNotNull(outcome)
        assertEquals(MoveType.STEP, outcome!!.type)
        assertEquals(pos(3, 5), game.state.pieces.first { it.id == 0 }.position)
    }

    @Test
    fun blockedStepIsRejected() {
        val game = gameOf(piece(0, p1, 7, 0), piece(1, p1, 6, 0), piece(8, p2, 0, 7))
        game.selectPiece(0)
        assertFalse(pos(6, 0) in game.state.legalStepMoves)
        assertNull(game.moveSelectedPiece(pos(6, 0)))
        assertNull(game.moveSelectedPiece(pos(3, 3)))
    }

    @Test
    fun horizontalJump() = assertSingleJump(middle = pos(4, 5), destination = pos(4, 6))

    @Test
    fun verticalJump() = assertSingleJump(middle = pos(3, 4), destination = pos(2, 4))

    @Test
    fun diagonalJump() = assertSingleJump(middle = pos(3, 3), destination = pos(2, 2))

    private fun assertSingleJump(middle: GridPosition, destination: GridPosition) {
        val game = gameOf(piece(0, p1, 4, 4), piece(8, p2, middle.row, middle.col))
        game.selectPiece(0)
        assertEquals(setOf(destination), game.state.legalJumpMoves)
        val outcome = game.moveSelectedPiece(destination)!!
        assertEquals(MoveType.JUMP, outcome.type)
        assertEquals(destination, game.state.pieces.first { it.id == 0 }.position)
    }

    @Test
    fun jumpOutsideBoardIsRejected() {
        val game = gameOf(piece(0, p1, 7, 1), piece(1, p1, 7, 0), piece(8, p2, 0, 7))
        game.selectPiece(0)
        assertTrue(game.state.legalJumpMoves.isEmpty())
    }

    @Test
    fun jumpOverOwnPiece() {
        val game = gameOf(piece(0, p1, 7, 0), piece(1, p1, 6, 1), piece(8, p2, 0, 7))
        game.selectPiece(0)
        assertTrue(pos(5, 2) in game.state.legalJumpMoves)
        assertEquals(MoveType.JUMP, game.moveSelectedPiece(pos(5, 2))!!.type)
    }

    @Test
    fun jumpOverOpponentPieceDoesNotCapture() {
        val game = gameOf(piece(0, p1, 4, 4), piece(8, p2, 4, 5), piece(9, p2, 0, 7))
        game.selectPiece(0)
        game.moveSelectedPiece(pos(4, 6))
        assertEquals(3, game.state.pieces.size)
        assertEquals(pos(4, 5), game.state.pieces.first { it.id == 8 }.position)
    }

    @Test
    fun multiJumpKeepsTurnAndSelection() {
        val game = gameOf(
            piece(0, p1, 6, 0), piece(1, p1, 5, 1), piece(8, p2, 3, 3), piece(9, p2, 0, 7)
        )
        game.selectPiece(0)
        val first = game.moveSelectedPiece(pos(4, 2))!!
        assertTrue(first.chainContinues)
        assertTrue(game.state.isJumpChain)
        assertEquals(p1, game.state.currentPlayer)
        assertEquals(0, game.state.selectedPieceId)
        assertTrue(game.state.legalStepMoves.isEmpty())
        // Switching pieces mid-chain is not allowed.
        assertFalse(game.selectPiece(1))
        assertFalse(game.deselect())
        assertFalse(game.canUndo)

        val second = game.moveSelectedPiece(pos(2, 4))!!
        assertFalse(second.chainContinues)
        assertEquals(2, game.state.totalJumps)
        assertEquals(1, game.state.turns)
        assertEquals(p2, game.state.currentPlayer)
    }

    @Test
    fun visitedJumpPositionIsRejected() {
        // After jumping 4,4 -> 4,6 over 4,5, jumping straight back to 4,4 would revisit the origin.
        val game = gameOf(piece(0, p1, 4, 4), piece(8, p2, 4, 5), piece(9, p2, 3, 6), piece(10, p2, 0, 0))
        game.selectPiece(0)
        game.moveSelectedPiece(pos(4, 6))
        assertTrue(game.state.isJumpChain)
        assertFalse(pos(4, 4) in game.state.legalJumpMoves)
        assertTrue(pos(2, 6) in game.state.legalJumpMoves)
        assertNull(game.moveSelectedPiece(pos(4, 4)))
    }

    @Test
    fun turnSwitchesAfterStep() {
        val game = CornerRaceGame()
        val piece = game.state.pieces.first { it.position == pos(4, 0) }
        game.selectPiece(piece.id)
        game.moveSelectedPiece(pos(3, 0))
        assertEquals(p2, game.state.currentPlayer)
        assertEquals(1, game.state.turns)
        assertNull(game.state.selectedPieceId)
    }

    @Test
    fun cannotSelectOpponentPiece() {
        val game = CornerRaceGame()
        val opponent = game.state.pieces.first { it.player == p2 }
        assertFalse(game.selectPiece(opponent.id))
    }

    @Test
    fun endTurnSwitchesPlayerAfterJumpChain() {
        val game = gameOf(piece(0, p1, 4, 4), piece(8, p2, 4, 5), piece(9, p2, 3, 6), piece(10, p2, 0, 0))
        assertFalse(game.endJumpTurn())
        game.selectPiece(0)
        game.moveSelectedPiece(pos(4, 6))
        assertTrue(game.endJumpTurn())
        assertEquals(p2, game.state.currentPlayer)
        assertFalse(game.state.isJumpChain)
        assertTrue(game.state.visitedJumpPositions.isEmpty())
        assertNull(game.state.selectedPieceId)
        assertEquals(1, game.state.turns)
    }

    @Test
    fun targetZoneCounting() {
        val target = BoardRules.targetZone(p1).toList()
        val pieces = target.take(5).mapIndexed { i, p -> CornerPiece(i, p1, p) } +
            listOf(piece(5, p1, 4, 4), piece(6, p1, 4, 3), piece(7, p1, 3, 3))
        assertEquals(5, BoardRules.piecesInTarget(p1, pieces))
        assertEquals(0, BoardRules.piecesInTarget(p2, pieces))
    }

    @Test
    fun winWhenAllEightPiecesEnterTarget() {
        val target = BoardRules.targetZone(p1).toList()
        val last = target.last()
        val p1Pieces = target.dropLast(1).mapIndexed { i, p -> CornerPiece(i, p1, p) } +
            CornerPiece(7, p1, GridPosition(last.row + 1, last.col))
        val game = gameOf(*(p1Pieces + piece(8, p2, 7, 7)).toTypedArray())
        assertFalse(game.hasWon(p1))
        game.selectPiece(7)
        val outcome = game.moveSelectedPiece(last)!!
        assertTrue(outcome.enteredTarget)
        assertEquals(p1, outcome.winner)
        assertTrue(game.hasWon(p1))
        assertEquals(p1, game.state.winner)
        // No input after the game is won.
        assertFalse(game.selectPiece(0))
        assertFalse(game.canUndo)
    }

    @Test
    fun undoRestoresStateBeforeWholeJumpSequence() {
        val game = gameOf(
            piece(0, p1, 6, 0), piece(1, p1, 5, 1), piece(8, p2, 3, 3), piece(9, p2, 0, 7)
        )
        val before = game.state
        game.selectPiece(0)
        game.moveSelectedPiece(pos(4, 2))
        game.moveSelectedPiece(pos(2, 4))
        assertTrue(game.canUndo)
        assertTrue(game.undo())
        assertEquals(before, game.state)
        assertFalse(game.canUndo)
    }

    @Test
    fun animationLocksInput() {
        val game = CornerRaceGame()
        game.setAnimating(true)
        assertFalse(game.selectPiece(0))
        game.setAnimating(false)
        assertTrue(game.selectPiece(0))
    }
}
