package com.github.irmin.elipticcurves.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.github.irmin.elipticcurves.domain.ECPoint
import com.github.irmin.elipticcurves.domain.EllipticCurve
import com.github.irmin.elipticcurves.domain.ModArithmetic
import com.github.irmin.elipticcurves.presentation.formatting.toCompactLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ScalarMultiplicationRow(
    val pointLabel: String,
    val multiples: List<String>,
    val isGenerator: Boolean
)

data class ScalarMultiplicationTableState(
    val aInput: String = "",
    val bInput: String = "",
    val pInput: String = "",
    val multipliers: List<Int> = emptyList(),
    val rows: List<ScalarMultiplicationRow> = emptyList(),
    val curveOrder: Int = 0,
    val errorMessage: String? = null,
    val calculated: Boolean = false
)

class ScalarMultiplicationTableViewModel : ViewModel() {

    private val _state = MutableStateFlow(ScalarMultiplicationTableState())
    val state: StateFlow<ScalarMultiplicationTableState> = _state.asStateFlow()

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
        val discriminant = curve.discriminant()
        if (discriminant == 0L) {
            _state.update { it.copy(errorMessage = "La curva es singular (discriminante ≡ 0 mod p). Por favor elige otros valores.") }
            return
        }

        val points = buildList {
            add(ECPoint.Infinity)
            for (x in 0 until p.toInt()) {
                val ex = curve.rhs(x.toLong()).toInt()
                for (y in 0 until p.toInt()) {
                    val ySquaredMod = ModArithmetic.modPow(y.toLong(), 2, p).toInt()
                    if (ySquaredMod == ex) {
                        add(ECPoint.Affine(x.toLong(), y.toLong()))
                    }
                }
            }
        }

        val n = points.size
        val multipliers = if (n >= 2) (2..n).toList() else emptyList()

        val rows = points.map { basePoint ->
            val isGenerator = isGenerator(curve = curve, basePoint = basePoint, groupOrder = n)
            val multiples = computeMultiples(curve = curve, basePoint = basePoint, groupOrder = n)
                .map { it.toCompactLabel() }

            ScalarMultiplicationRow(
                pointLabel = basePoint.toCompactLabel(),
                multiples = multiples,
                isGenerator = isGenerator
            )
        }

        _state.update {
            it.copy(
                multipliers = multipliers,
                rows = rows,
                curveOrder = n,
                errorMessage = null,
                calculated = true
            )
        }
    }

    private fun computeMultiples(curve: EllipticCurve, basePoint: ECPoint, groupOrder: Int): List<ECPoint> {
        if (groupOrder < 2) return emptyList()

        val results = ArrayList<ECPoint>(groupOrder - 1)
        var current: ECPoint = basePoint
        for (k in 2..groupOrder) {
            current = curve.add(current, basePoint)
            results.add(current)
        }
        return results
    }

    private fun isGenerator(curve: EllipticCurve, basePoint: ECPoint, groupOrder: Int): Boolean {
        if (basePoint === ECPoint.Infinity) return false
        if (groupOrder <= 1) return false

        var count = 1
        var current: ECPoint = basePoint
        while (current !== ECPoint.Infinity) {
            current = curve.add(current, basePoint)
            count++
            if (count > groupOrder) break
        }
        return count == groupOrder
    }
}
