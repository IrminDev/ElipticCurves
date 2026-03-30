package com.github.irmin.elipticcurves.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.github.irmin.elipticcurves.domain.ECPoint
import com.github.irmin.elipticcurves.domain.EllipticCurve
import com.github.irmin.elipticcurves.domain.ModArithmetic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CurveTableRow(
    val x: Int,
    val ex: Int,
    val ySquared: Int,
    val yValues: List<Int>
)

data class EllipticCurveState(
    val aInput: String = "",
    val bInput: String = "",
    val pInput: String = "",
    val tableRows: List<CurveTableRow> = emptyList(),
    val points: List<Pair<Int, Int>> = emptyList(),
    val generators: Set<Pair<Int, Int>> = emptySet(),
    val errorMessage: String? = null,
    val calculated: Boolean = false
)

class EllipticCurveViewModel : ViewModel() {

    private val _state = MutableStateFlow(EllipticCurveState())
    val state: StateFlow<EllipticCurveState> = _state.asStateFlow()

    fun onAChanged(value: String) {
        _state.update { it.copy(aInput = value, errorMessage = null, calculated = false) }
    }

    fun onBChanged(value: String) {
        _state.update { it.copy(bInput = value, errorMessage = null, calculated = false) }
    }

    fun onPChanged(value: String) {
        _state.update { it.copy(pInput = value, errorMessage = null, calculated = false) }
    }

    fun calculate() {
        val aText = _state.value.aInput.trim()
        val bText = _state.value.bInput.trim()
        val pText = _state.value.pInput.trim()

        val a = aText.toLongOrNull()
        val b = bText.toLongOrNull()
        val p = pText.toLongOrNull()

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

        val rows = mutableListOf<CurveTableRow>()
        val points = mutableListOf<Pair<Int, Int>>()

        points.add(Pair(Int.MAX_VALUE, Int.MAX_VALUE))

        for (x in 0 until p.toInt()) {
            val xLong = x.toLong()
            val ex = curve.rhs(xLong).toInt()

            val yValues = mutableListOf<Int>()
            for (y in 0 until p.toInt()) {
                val ySquaredMod = ModArithmetic.modPow(y.toLong(), 2, p).toInt()
                if (ySquaredMod == ex) {
                    yValues.add(y)
                    points.add(Pair(x, y))
                }
            }

            rows.add(
                CurveTableRow(
                    x = x,
                    ex = ex,
                    ySquared = ex,
                    yValues = yValues
                )
            )
        }
        val totalPointsCount = points.size
        val generators = mutableSetOf<Pair<Int, Int>>()

        for (point in points) {
            if (point.first == Int.MAX_VALUE) continue

            val px = point.first.toLong()
            val py = point.second.toLong()
            val basePoint: ECPoint = ECPoint.Affine(px, py)

            var count = 1
            var currentPoint: ECPoint = basePoint

            while (currentPoint !== ECPoint.Infinity) {
                currentPoint = curve.add(currentPoint, basePoint)
                count++
                if (count > totalPointsCount) break
            }

            if (count == totalPointsCount) {
                generators.add(point)
            }
        }

        _state.update {
            it.copy(
                tableRows = rows,
                points = points,
                generators = generators,
                errorMessage = null,
                calculated = true
            )
        }
    }
}
