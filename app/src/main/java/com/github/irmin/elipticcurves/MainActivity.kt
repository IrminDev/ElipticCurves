package com.github.irmin.elipticcurves

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.irmin.elipticcurves.ui.theme.ElipticCurvesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElipticCurvesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EllipticCurveScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun EllipticCurveScreen(
    modifier: Modifier = Modifier,
    viewModel: EllipticCurveViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val verticalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(verticalScroll)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Curva Elíptica",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "y^2 ≡ x^3 + ax + b (mod p)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        // Input card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Parámetros de la curva",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.aInput,
                        onValueChange = viewModel::onAChanged,
                        label = { Text("a") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.bInput,
                        onValueChange = viewModel::onBChanged,
                        label = { Text("b") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.pInput,
                        onValueChange = viewModel::onPChanged,
                        label = { Text("p (primo)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = viewModel::calculate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Calcular puntos")
                }
            }
        }

        // Error message
        state.errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Results
        if (state.calculated) {
            val a = state.aInput.trim().toLongOrNull() ?: 0L
            val b = state.bInput.trim().toLongOrNull() ?: 0L
            val p = state.pInput.trim().toLongOrNull() ?: 0L

            Text(
                text = "E: y^2 ≡ x^3 + ${a}x + $b (mod $p)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Tabla de evaluación",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        CurveTable(rows = state.tableRows, p = p)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val realPoints = state.points.filter { it.first != Int.MAX_VALUE }
                    Text(
                        text = "Conjunto de puntos de la curva",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "|E| = ${realPoints.size + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))

                    val pointsText = buildString {
                        append("{ O")
                        realPoints.forEach { (x, y) -> append(", ($x, $y)") }
                        append(" }")
                    }
                    Text(
                        text = pointsText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun CurveTable(rows: List<CurveTableRow>, p: Long) {
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

                val pointsText = if (row.yValues.isNotEmpty())
                    row.yValues.joinToString("\n") { y -> "(${row.x}, $y)" }
                else "—"
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
            fontSize = if (isHeader) 12.sp else 12.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            color = fg,
            lineHeight = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EllipticCurveScreenPreview() {
    ElipticCurvesTheme {
        EllipticCurveScreen()
    }
}