package com.github.irmin.elipticcurves.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.irmin.elipticcurves.presentation.viewmodel.CurveTableRow

@Composable
fun CurveTable(rows: List<CurveTableRow>, p: Long, generators: Set<Pair<Int, Int>>) {
    val headerBg = MaterialTheme.colorScheme.primaryContainer
    val headerFg = MaterialTheme.colorScheme.onPrimaryContainer
    val altRowBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val highlightBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Column {
        Row {
            TableCell(text = "x", isHeader = true, bg = headerBg, fg = headerFg, borderColor = borderColor, width = 56)
            TableCell(text = "x^3+ax+b mod p\n(E(x))", isHeader = true, bg = headerBg, fg = headerFg, borderColor = borderColor, width = 140)
            TableCell(text = "y^2 mod p", isHeader = true, bg = headerBg, fg = headerFg, borderColor = borderColor, width = 90)
            TableCell(text = "y (si existe)", isHeader = true, bg = headerBg, fg = headerFg, borderColor = borderColor, width = 110)
            TableCell(text = "Puntos", isHeader = true, bg = headerBg, fg = headerFg, borderColor = borderColor, width = 160)
        }

        rows.forEach { row ->
            val hasPoints = row.yValues.isNotEmpty()
            val rowBg = if (hasPoints) highlightBg else if (row.x % 2 == 0) altRowBg else Color.Transparent

            Row(modifier = Modifier.background(rowBg)) {
                TableCell(text = row.x.toString(), bg = Color.Transparent, borderColor = borderColor, width = 56)
                TableCell(text = row.ex.toString(), bg = Color.Transparent, borderColor = borderColor, width = 140)

                val ySquaredList = if (row.yValues.isNotEmpty())
                    row.yValues.joinToString("\n") { y -> "${y}²≡${row.ex}" }
                else "—"
                TableCell(text = ySquaredList, bg = Color.Transparent, borderColor = borderColor, width = 90)

                val yText = if (row.yValues.isNotEmpty()) row.yValues.joinToString(", ") else "—"
                TableCell(text = yText, bg = Color.Transparent, borderColor = borderColor, width = 110)

                val pointsText = if (row.yValues.isNotEmpty()) {
                    row.yValues.joinToString("\n") { y ->
                        val pointPair = Pair(row.x, y)
                        if (generators.contains(pointPair)) {
                            "(${row.x}, $y) ⭐"
                        } else {
                            "(${row.x}, $y)"
                        }
                    }
                } else "—"
                TableCell(
                    text = pointsText,
                    bg = Color.Transparent,
                    borderColor = borderColor,
                    width = 160,
                    fontWeight = if (hasPoints) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    isHeader: Boolean = false,
    bg: Color,
    fg: Color = Color.Unspecified,
    borderColor: Color,
    width: Int,
    fontWeight: FontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
) {
    Box(
        modifier = Modifier
            .width(width.dp)
            .border(0.5.dp, borderColor)
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            color = fg,
            lineHeight = 16.sp
        )
    }
}
