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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.irmin.elipticcurves.ui.theme.ElipticCurvesTheme

// ── Navigation routes ──────────────────────────────────────────────────────────
private object Routes {
    const val MENU = "menu"
    const val CURVE_POINTS = "curve_points"
    const val POINT_SUM = "point_sum"
    const val POINT_MUL = "point_mul"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElipticCurvesTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Routes.MENU) {
                    composable(Routes.MENU) {
                        MenuScreen(
                            onNavigateToCurvePoints = { navController.navigate(Routes.CURVE_POINTS) },
                            onNavigateToPointSum = { navController.navigate(Routes.POINT_SUM) },
                            onNavigateToMultiplication = { navController.navigate(Routes.POINT_MUL) }
                        )
                    }
                    composable(Routes.CURVE_POINTS) {
                        EllipticCurveScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.POINT_SUM) {
                        PointSumScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.POINT_MUL) {
                        PointMultiplicationScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

// ── Menu Screen ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigateToCurvePoints: () -> Unit,
    onNavigateToPointSum: () -> Unit,
    onNavigateToMultiplication: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Curvas Elípticas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = "Herramientas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            MenuButton(
                title = "Puntos de la Curva",
                subtitle = "Calcula el conjunto de puntos E(a,b,p)\nmediante fuerza bruta",
                onClick = onNavigateToCurvePoints
            )

            MenuButton(
                title = "Suma de Puntos (P + Q)",
                subtitle = "Calcula la suma de dos puntos\nsobre la curva elíptica",
                onClick = onNavigateToPointSum
            )

            MenuButton(
                title = "Mutliplicacion de Punto (kP)",
                subtitle = "Calcula la multiplicación de\nkP sobre la curva elípitica",
                onClick = onNavigateToMultiplication
            )
        }
    }
}

@Composable
private fun MenuButton(title: String, subtitle: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

// ── Elliptic Curve Points Screen ───────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EllipticCurveScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EllipticCurveViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val verticalScroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puntos de la Curva") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
                .padding(innerPadding)
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
}

// ── Table composables ──────────────────────────────────────────────────────────
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
            fontSize = 12.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            color = fg,
            lineHeight = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    ElipticCurvesTheme {
        MenuScreen(onNavigateToCurvePoints = {}, onNavigateToPointSum = {},
            onNavigateToMultiplication = {})
    }
}

@Preview(showBackground = true)
@Composable
fun EllipticCurveScreenPreview() {
    ElipticCurvesTheme {
        EllipticCurveScreen(onBack = {})
    }
}