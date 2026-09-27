package com.example.cornerrace.ui.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.cornerrace.game.GridPosition
import com.example.cornerrace.game.Player
import com.example.cornerrace.ui.theme.BoardColors
import com.example.cornerrace.ui.theme.PieceColors

/** Drawing primitives shared by the game board, splash and rules illustrations. */

fun cellCenter(position: GridPosition, cell: Float): Offset =
    Offset((position.col + 0.5f) * cell, (position.row + 0.5f) * cell)

fun DrawScope.drawCells(boardSize: Int, cell: Float, colors: BoardColors) {
    for (row in 0 until boardSize) {
        for (col in 0 until boardSize) {
            drawRect(
                color = if ((row + col) % 2 == 0) colors.cellLight else colors.cellDark,
                topLeft = Offset(col * cell, row * cell),
                size = Size(cell, cell)
            )
        }
    }
    val stroke = cell * 0.012f
    for (i in 1 until boardSize) {
        drawLine(colors.gridLine, Offset(i * cell, 0f), Offset(i * cell, boardSize * cell), stroke)
        drawLine(colors.gridLine, Offset(0f, i * cell), Offset(boardSize * cell, i * cell), stroke)
    }
}

/** Shades a zone and outlines its outer boundary so the corner reads as one shape. */
fun DrawScope.drawZone(zone: Set<GridPosition>, cell: Float, color: Color, fillAlpha: Float, edgeAlpha: Float) {
    for (p in zone) {
        drawRect(color.copy(alpha = fillAlpha), Offset(p.col * cell, p.row * cell), Size(cell, cell))
    }
    val edge = color.copy(alpha = edgeAlpha)
    val w = cell * 0.035f
    val inset = w / 2
    for (p in zone) {
        val left = p.col * cell
        val top = p.row * cell
        val right = left + cell
        val bottom = top + cell
        if (GridPosition(p.row - 1, p.col) !in zone) drawLine(edge, Offset(left, top + inset), Offset(right, top + inset), w)
        if (GridPosition(p.row + 1, p.col) !in zone) drawLine(edge, Offset(left, bottom - inset), Offset(right, bottom - inset), w)
        if (GridPosition(p.row, p.col - 1) !in zone) drawLine(edge, Offset(left + inset, top), Offset(left + inset, bottom), w)
        if (GridPosition(p.row, p.col + 1) !in zone) drawLine(edge, Offset(right - inset, top), Offset(right - inset, bottom), w)
    }
}

fun DrawScope.drawStepHint(center: Offset, cell: Float, color: Color) {
    drawCircle(color.copy(alpha = 0.55f), radius = cell * 0.085f, center = center)
}

fun DrawScope.drawJumpHint(center: Offset, cell: Float, color: Color) {
    drawCircle(color.copy(alpha = 0.12f), radius = cell * 0.25f, center = center)
    drawCircle(color.copy(alpha = 0.85f), radius = cell * 0.25f, center = center, style = Stroke(cell * 0.045f))
}

private fun diamondPath(center: Offset, radius: Float, rounding: Float = 0.2f): Path {
    val v = listOf(
        Offset(center.x, center.y - radius),
        Offset(center.x + radius, center.y),
        Offset(center.x, center.y + radius),
        Offset(center.x - radius, center.y)
    )
    return Path().apply {
        for (i in 0..3) {
            val cur = v[i]
            val prev = v[(i + 3) % 4]
            val next = v[(i + 1) % 4]
            val a = cur + (prev - cur) * rounding
            val b = cur + (next - cur) * rounding
            if (i == 0) moveTo(a.x, a.y) else lineTo(a.x, a.y)
            quadraticTo(cur.x, cur.y, b.x, b.y)
        }
        close()
    }
}

private fun DrawScope.drawTokenShape(player: Player, center: Offset, radius: Float, brush: Brush, style: DrawStyle = Fill) {
    if (player == Player.PLAYER_ONE) {
        // Diamonds carry less area than circles of the same radius, so extend the vertices.
        drawPath(diamondPath(center, radius * 1.16f), brush, style = style)
    } else {
        drawCircle(brush, radius = radius, center = center, style = style)
    }
}

private fun DrawScope.drawTokenShape(player: Player, center: Offset, radius: Float, color: Color, style: DrawStyle = Fill) =
    drawTokenShape(player, center, radius, SolidColor(color), style)

/**
 * Draws a beveled token. [lift] in 0..1 raises the body and spreads the shadow; [arc] adds extra height during jumps.
 */
fun DrawScope.drawToken(
    player: Player,
    center: Offset,
    cell: Float,
    colors: PieceColors,
    lift: Float = 0f,
    arc: Float = 0f,
    selected: Boolean = false,
    ringColor: Color = Color.White,
    alpha: Float = 1f
) {
    val height = lift * 0.07f + arc * 0.32f
    val radius = cell * 0.34f * (1f + 0.05f * lift + 0.1f * arc)
    val body = Offset(center.x, center.y - cell * height)

    // Soft layered shadow that stays on the board and fades as the piece rises.
    val shadowCenter = Offset(center.x + cell * 0.02f, center.y + cell * (0.06f + 0.04f * lift))
    val shadowFade = (1f - 0.5f * arc) * alpha
    for (i in 0..3) {
        drawTokenShape(
            player, shadowCenter, radius * (1f + 0.06f * i + 0.12f * arc),
            Color.Black.copy(alpha = (0.15f - i * 0.03f) * shadowFade)
        )
    }

    // Rim, then beveled face.
    drawTokenShape(player, body, radius, colors.deep.copy(alpha = alpha))
    val face = Brush.linearGradient(
        colors = listOf(colors.light.copy(alpha = alpha), colors.base.copy(alpha = alpha), colors.deep.copy(alpha = alpha)),
        start = Offset(body.x - radius, body.y - radius),
        end = Offset(body.x + radius, body.y + radius)
    )
    drawTokenShape(player, body, radius * 0.88f, face)

    // Inner highlight ring along the bevel.
    val highlight = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.38f * alpha), Color.White.copy(alpha = 0.0f)),
        start = Offset(body.x - radius, body.y - radius),
        end = Offset(body.x + radius * 0.4f, body.y + radius * 0.4f)
    )
    drawTokenShape(player, body, radius * 0.86f, highlight, Stroke(cell * 0.018f))

    // Engraved symbol: a recessed shape with a light lower edge.
    val symbol = radius * 0.36f
    val engraveDark = colors.deep.copy(alpha = 0.7f * alpha)
    val engraveLight = Color.White.copy(alpha = 0.22f * alpha)
    val symbolStroke = Stroke(radius * 0.11f)
    val offsetLight = Offset(0f, radius * 0.04f)
    if (player == Player.PLAYER_ONE) {
        drawPath(diamondPath(body + offsetLight, symbol * 1.1f), engraveLight, style = symbolStroke)
        drawPath(diamondPath(body, symbol * 1.1f), engraveDark, style = symbolStroke)
    } else {
        drawCircle(engraveLight, radius = symbol, center = body + offsetLight, style = symbolStroke)
        drawCircle(engraveDark, radius = symbol, center = body, style = symbolStroke)
    }

    if (selected) {
        drawTokenShape(player, body, radius * 1.2f, ringColor.copy(alpha = 0.9f * alpha), Stroke(cell * 0.03f))
    }
}
