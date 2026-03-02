package com.github.irmin.elipticcurves

import androidx.lifecycle.ViewModel
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

        if (!isPrime(p)) {
            _state.update { it.copy(errorMessage = "p = $p no es primo. Por favor ingresa un número primo.") }
            return
        }

        // Check curve is non-singular: 4a^3 + 27b^2 != 0 (mod p)
        val discriminant = (4L * modPow(a, 3, p) + 27L * modMul(b, b, p)) % p
        if (discriminant != 0L) {
            _state.update { it.copy(errorMessage = "La curva es singular (discriminante ≡ 0 mod p). Por favor elige otros valores.") }
            return
        }

        val rows = mutableListOf<CurveTableRow>()
        val points = mutableListOf<Pair<Int, Int>>()

        // Add infinite point at the end
        points.add(Pair(Int.MAX_VALUE, Int.MAX_VALUE))

        for (x in 0 until p.toInt()) {
            val xLong = x.toLong()
            val ex = ((modPow(xLong, 3, p) + modMul(a, xLong, p) + b).mod(p)).toInt()

            val yValues = mutableListOf<Int>()
            for (y in 0 until p.toInt()) {
                val ySquaredMod = modPow(y.toLong(), 2, p).toInt()
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

        _state.update {
            it.copy(
                tableRows = rows,
                points = points,
                errorMessage = null,
                calculated = true
            )
        }
    }

    // Simple primality test (sufficient for small p)
    private fun isPrime(n: Long): Boolean {
        if (n < 2) return false
        if (n == 2L) return true
        if (n % 2 == 0L) return false
        val sqrt = Math.sqrt(n.toDouble()).toLong()
        for (i in 3..sqrt step 2) {
            if (n % i == 0L) return false
        }
        return true
    }

    // Fast modular exponentiation
    private fun modPow(base: Long, exp: Long, mod: Long): Long {
        var result = 1L
        var b = base.mod(mod)
        var e = exp
        while (e > 0) {
            if ((e and 1) == 1L) result = result * b % mod
            b = b * b % mod
            e = e shr 1
        }
        return result
    }

    // Modular multiplication
    private fun modMul(a: Long, b: Long, mod: Long): Long {
        return (a.mod(mod)) * (b.mod(mod)) % mod
    }
}

