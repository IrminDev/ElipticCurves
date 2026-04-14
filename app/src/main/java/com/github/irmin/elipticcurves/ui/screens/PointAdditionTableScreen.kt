package com.github.irmin.elipticcurves.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.irmin.elipticcurves.presentation.viewmodel.PointAdditionTableViewModel
import com.github.irmin.elipticcurves.ui.components.TableCell

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointAdditionTableScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PointAdditionTableViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tabla de Suma de Puntos") },
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
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "y² ≡ x³ + ax + b (mod p)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
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
                        Text("Calcular tabla")
                    }
                }
            }

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

            if (state.calculated) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        val headerBg = MaterialTheme.colorScheme.primaryContainer
                        val headerFg = MaterialTheme.colorScheme.onPrimaryContainer
                        val altRowBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Column {
                                Row {
                                    TableCell(
                                        text = "P + Q",
                                        isHeader = true,
                                        bg = headerBg,
                                        fg = headerFg,
                                        borderColor = borderColor,
                                        width = 140
                                    )
                                    state.pointLabels.forEach { label ->
                                        TableCell(
                                            text = label,
                                            isHeader = true,
                                            bg = headerBg,
                                            fg = headerFg,
                                            borderColor = borderColor,
                                            width = 120
                                        )
                                    }
                                }

                                state.pointLabels.forEachIndexed { i, rowLabel ->
                                    val rowBg: Color = if (i % 2 == 0) altRowBg else Color.Transparent
                                    Row(modifier = Modifier.background(rowBg)) {
                                        TableCell(
                                            text = rowLabel,
                                            isHeader = true,
                                            bg = headerBg,
                                            fg = headerFg,
                                            borderColor = borderColor,
                                            width = 140
                                        )
                                        state.table.getOrNull(i).orEmpty().forEach { value ->
                                            TableCell(
                                                text = value,
                                                bg = Color.Transparent,
                                                borderColor = borderColor,
                                                width = 120
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
