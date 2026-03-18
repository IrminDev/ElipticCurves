package com.github.irmin.elipticcurves

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PointSumState(
    val aInput: String = "",
    val bInput: String = "",
    val pInput: String = "",
    val px1Input: String = "",
    val py1Input: String = "",
    val px2Input: String = "",
    val py2Input: String = "",
    val resultPoint: String? = null,
    val errorMessage: String? = null,
    val calculated: Boolean = false,
    val p1IsInfinity: Boolean = false,
    val p2IsInfinity: Boolean = false
)

class PointSumViewModel : ViewModel() {

    private val _state = MutableStateFlow(PointSumState())
    val state: StateFlow<PointSumState> = _state.asStateFlow()

    fun onAChanged(value: String) { _state.update { it.copy(aInput = value, errorMessage = null, calculated = false) } }
    fun onBChanged(value: String) { _state.update { it.copy(bInput = value, errorMessage = null, calculated = false) } }
    fun onPChanged(value: String) { _state.update { it.copy(pInput = value, errorMessage = null, calculated = false) } }
    fun onPx1Changed(value: String) { _state.update { it.copy(px1Input = value, errorMessage = null, calculated = false) } }
    fun onPy1Changed(value: String) { _state.update { it.copy(py1Input = value, errorMessage = null, calculated = false) } }
    fun onPx2Changed(value: String) { _state.update { it.copy(px2Input = value, errorMessage = null, calculated = false) } }
    fun onPy2Changed(value: String) { _state.update { it.copy(py2Input = value, errorMessage = null, calculated = false) } }
    fun onP1IsInfinityChanged(value: Boolean) { _state.update { it.copy(p1IsInfinity = value, errorMessage = null, calculated = false) } }
    fun onP2IsInfinityChanged(value: Boolean) { _state.update { it.copy(p2IsInfinity = value, errorMessage = null, calculated = false) } }

    fun calculate() {
        val s = _state.value
        val a = s.aInput.trim().toLongOrNull()
        val b = s.bInput.trim().toLongOrNull()
        val p = s.pInput.trim().toLongOrNull()

        if (a == null || b == null || p == null) {
            _state.update { it.copy(errorMessage = "Por favor ingresa valores numéricos válidos para a, b y p.") }
            return
        }
        if (p < 2 || !isPrime(p)) {
            _state.update { it.copy(errorMessage = "p debe ser un número primo mayor o igual a 2.") }
            return
        }

        // Parse points (O = punto al infinito)
        val p1IsInf = s.p1IsInfinity
        val p2IsInf = s.p2IsInfinity

        val x1: Long?
        val y1: Long?
        val x2: Long?
        val y2: Long?

        if (!p1IsInf) {
            x1 = s.px1Input.trim().toLongOrNull()
            y1 = s.py1Input.trim().toLongOrNull()
            if (x1 == null || y1 == null) {
                _state.update { it.copy(errorMessage = "Coordenadas del punto P inválidas.") }
                return
            }
            if (!isOnCurve(x1, y1, a, b, p)) {
                _state.update { it.copy(errorMessage = "El punto P($x1, $y1) no pertenece a la curva.") }
                return
            }
        } else { x1 = null; y1 = null }

        if (!p2IsInf) {
            x2 = s.px2Input.trim().toLongOrNull()
            y2 = s.py2Input.trim().toLongOrNull()
            if (x2 == null || y2 == null) {
                _state.update { it.copy(errorMessage = "Coordenadas del punto Q inválidas.") }
                return
            }
            if (!isOnCurve(x2, y2, a, b, p)) {
                _state.update { it.copy(errorMessage = "El punto Q($x2, $y2) no pertenece a la curva.") }
                return
            }
        } else { x2 = null; y2 = null }

        val result = addPoints(x1, y1, x2, y2, a, p)

        val resultStr = if (result == null) "O (Punto al infinito)"
        else "(${result.first}, ${result.second})"

        _state.update { it.copy(resultPoint = resultStr, errorMessage = null, calculated = true) }
    }

    private fun isOnCurve(x: Long, y: Long, a: Long, b: Long, p: Long): Boolean {
        val lhs = modPow(y, 2, p)
        val rhs = (modPow(x, 3, p) + a.mod(p) * x.mod(p) % p + b.mod(p)).mod(p)
        return lhs == rhs
    }

    /**
     * Returns null for point at infinity, or Pair(x, y) for a finite point.
     * P = (x1, y1), Q = (x2, y2). null means infinity.
     */
    private fun addPoints(
        x1: Long?, y1: Long?,
        x2: Long?, y2: Long?,
        a: Long, p: Long
    ): Pair<Long, Long>? {
        // P = O  =>  P + Q = Q
        if (x1 == null) return if (x2 == null) null else Pair(x2, y2!!)
        // Q = O  =>  P + Q = P
        if (x2 == null) return Pair(x1, y1!!)

        // Normalize values mod p
        val x1m = x1.mod(p)
        val y1m = y1!!.mod(p)
        val x2m = x2.mod(p)
        val y2m = y2!!.mod(p)

        // P = -Q  (same x, y1 + y2 = 0 mod p)
        if (x1m == x2m && (y1m + y2m) % p == 0L) return null

        val lambda: Long = if (x1m == x2m && y1m == y2m) {
            // Point doubling: λ = (3*x1^2 + a) / (2*y1) mod p
            if (y1m == 0L) return null
            val num = (3L * modPow(x1m, 2, p) + a.mod(p)).mod(p)
            val den = (2L * y1m).mod(p)
            if (den == 0L) return null
            num * inverseMod(den, p) % p
        } else {
            // Point addition: λ = (y2 - y1) / (x2 - x1) mod p
            val num = (y2m - y1m + p).mod(p)
            val den = (x2m - x1m + p).mod(p)
            if (den == 0L) return null
            num * inverseMod(den, p) % p
        }

        val x3 = (modPow(lambda, 2, p) - x1m - x2m + p * 2).mod(p)
        val y3 = (lambda * (x1m - x3 + p * 2).mod(p) - y1m + p * 2).mod(p)

        return Pair(x3, y3)
    }

    private fun inverseMod(a: Long, p: Long): Long {
        // Asumimos p primo (se valida antes); usar Fermat: a^(p-2) mod p
        val am = ((a % p) + p) % p
        return modPow(am, p - 2, p)
    }

    private fun modPow(base: Long, exp: Long, mod: Long): Long {
        var result = 1L
        var b = base.mod(mod)
        var e = exp
        while (e > 0) {
            if ((e and 1L) == 1L) result = result * b % mod
            b = b * b % mod
            e = e shr 1
        }
        return result
    }

    private fun isPrime(n: Long): Boolean {
        if (n < 2) return false
        if (n == 2L) return true
        if (n % 2 == 0L) return false
        val sqrt = Math.sqrt(n.toDouble()).toLong()
        for (i in 3..sqrt step 2) if (n % i == 0L) return false
        return true
    }
}
