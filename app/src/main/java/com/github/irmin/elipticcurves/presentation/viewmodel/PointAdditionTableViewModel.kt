package com.github.irmin.elipticcurves.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.github.irmin.elipticcurves.domain.ECPoint
import com.github.irmin.elipticcurves.domain.EllipticCurve
import com.github.irmin.elipticcurves.domain.ModArithmetic
import com.github.irmin.elipticcurves.domain.enumeratePoints
import com.github.irmin.elipticcurves.presentation.formatting.toCompactLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PointAdditionTableState(
    val aInput: String = "",
    val bInput: String = "",
    val pInput: String = "",
    val pointLabels: List<String> = emptyList(),
    val table: List<List<String>> = emptyList(),
    val errorMessage: String? = null,
    val calculated: Boolean = false
)

class PointAdditionTableViewModel : ViewModel() {

    private val _state = MutableStateFlow(PointAdditionTableState())
    val state: StateFlow<PointAdditionTableState> = _state.asStateFlow()

    fun onAChanged(value: String) { _state.update { it.copy(aInput = value, errorMessage = null, calculated = false) } }
    fun onBChanged(value: String) { _state.update { it.copy(bInput = value, errorMessage = null, calculated = false) } }
    fun onPChanged(value: String) { _state.update { it.copy(pInput = value, errorMessage = null, calculated = false) } }

    fun calculate() {
        val s = _state.value
        val a = s.aInput.trim().toLongOrNull()
        val b = s.bInput.trim().toLongOrNull()
        val p = s.pInput.trim().toLongOrNull()

        if (a == null || b == null || p == null) {
            _state.update { it.copy(errorMessage = "Por favor ingresa valores numéricos válidos para a, b y p.") }
            return
        }
        if (p < 2) {
            _state.update { it.copy(errorMessage = "p debe ser un número primo mayor o igual a 2.") }
            return
        }
        if (!ModArithmetic.isPrime(p)) {
            _state.update { it.copy(errorMessage = "p = $p no es primo. Por favor ingresa un número primo.") }
            return
        }

        val curve = EllipticCurve(a = a, b = b, p = p)
        if (!curve.isNonSingular()) {
            _state.update { it.copy(errorMessage = "La curva es singular (discriminante ≡ 0 mod p). Por favor elige otros valores.") }
            return
        }

        val points = curve.enumeratePoints()
        val pointLabels = points.map { it.toCompactLabel() }

        val table = points.map { rowPoint ->
            points.map { colPoint ->
                curve.add(rowPoint, colPoint).toCompactLabel()
            }
        }

        _state.update {
            it.copy(
                pointLabels = pointLabels,
                table = table,
                errorMessage = null,
                calculated = true
            )
        }
    }
}
