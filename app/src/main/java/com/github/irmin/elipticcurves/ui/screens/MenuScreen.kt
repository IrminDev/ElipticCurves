package com.github.irmin.elipticcurves.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.irmin.elipticcurves.ui.theme.ElipticCurvesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigateToCurvePoints: () -> Unit,
    onNavigateToPointSum: () -> Unit,
    onNavigateToMultiplication: () -> Unit,
    onNavigateToScalarTable: () -> Unit
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

            MenuButton(
                title = "Tabla de Multiplicación Escalar",
                subtitle = "Imprime kP para cada punto\ny múltiples escalares",
                onClick = onNavigateToScalarTable
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

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    ElipticCurvesTheme {
        MenuScreen(
            onNavigateToCurvePoints = {},
            onNavigateToPointSum = {},
            onNavigateToMultiplication = {},
            onNavigateToScalarTable = {}
        )
    }
}
