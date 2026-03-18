package com.github.irmin.elipticcurves

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PointMultiplicationState(
    val aInput: String = "",
    val bInput: String = "",
    val pInput: String = "",
    val kInput: String = "",
    val pxInput: String = "",
    val pyInput: String = "",
    val resultPoint: String? = null,
    val errorMessage: String? = null,
    val calculated: Boolean = false,
    val pIsInfinity: Boolean = false
)

class PointMultiplicationViewModel : ViewModel() {

    private val _state = MutableStateFlow(PointMultiplicationState())
    val state: StateFlow<PointMultiplicationState> = _state.asStateFlow()

    fun onAChanged(value: String) { _state.update { it.copy(aInput = value, errorMessage = null, calculated = false) } }
    fun onBChanged(value: String) { _state.update { it.copy(bInput = value, errorMessage = null, calculated = false) } }
    fun onPChanged(value: String) { _state.update { it.copy(pInput = value, errorMessage = null, calculated = false) } }
    fun onKChanged(value: String) { _state.update { it.copy(kInput = value, errorMessage = null, calculated = false) } }
    fun onPxChanged(value: String) { _state.update { it.copy(pxInput = value, errorMessage = null, calculated = false) } }
    fun onPyChanged(value: String) { _state.update { it.copy(pyInput = value, errorMessage = null, calculated = false) } }
    fun onPIsInfinityChanged(value: Boolean) { _state.update { it.copy(pIsInfinity = value, errorMessage = null, calculated = false) } }

    fun calculate() {
        val s = _state.value
        val a = s.aInput.trim().toLongOrNull()
        val b = s.bInput.trim().toLongOrNull()
        val p = s.pInput.trim().toLongOrNull()
        val k = s.kInput.trim().toLongOrNull()

        if (a == null || b == null || p == null || k == null) {
            _state.update { it.copy(errorMessage = "Por favor ingresa valores numéricos válidos para a, b, p y k.") }
            return
        }
        if (p < 2 || !isPrime(p)) {
            _state.update { it.copy(errorMessage = "p debe ser un número primo mayor o igual a 2.") }
            return
        }
        if (k < 0) {
            _state.update { it.copy(errorMessage = "k debe ser un entero no negativo.") }
            return
        }

        val pIsInf = s.pIsInfinity
        val x: Long?
        val y: Long?

        if (!pIsInf) {
            x = s.pxInput.trim().toLongOrNull()
            y = s.pyInput.trim().toLongOrNull()
            if (x == null || y == null) {
                _state.update { it.copy(errorMessage = "Coordenadas del punto P inválidas.") }
                return
            }
            if (!isOnCurve(x, y, a, b, p)) {
                _state.update { it.copy(errorMessage = "El punto P($x, $y) no pertenece a la curva.") }
                return
            }
        } else { x = null; y = null }

        val result = multiplyPoint(k, x, y, a, p)

        val resultStr = if (result == null) "O (Punto al infinito)"
        else "(${result.first}, ${result.second})"

        _state.update { it.copy(resultPoint = resultStr, errorMessage = null, calculated = true) }
    }

    private fun isOnCurve(x: Long, y: Long, a: Long, b: Long, p: Long): Boolean {
        val lhs = modPow(y, 2, p)
        val rhs = (modPow(x, 3, p) + a.mod(p) * x.mod(p) % p + b.mod(p)).mod(p)
        return lhs == rhs
    }

    private fun multiplyPoint(k: Long, x: Long?, y: Long?, a: Long, p: Long): Pair<Long, Long>? {
        if (k == 0L || x == null) return null
        
        var res: Pair<Long, Long>? = null
        var temp: Pair<Long, Long>? = Pair(x, y!!)
        var currentK = k
        
        while (currentK > 0) {
            if ((currentK and 1L) == 1L) {
                res = if (res == null) temp else addPoints(res.first, res.second, temp?.first, temp?.second, a, p)
            }
            temp = if (temp == null) null else addPoints(temp.first, temp.second, temp.first, temp.second, a, p)
            currentK = currentK shr 1
        }
        return res
    }

    private fun addPoints(
        x1: Long?, y1: Long?,
        x2: Long?, y2: Long?,
        a: Long, p: Long
    ): Pair<Long, Long>? {
        if (x1 == null) return if (x2 == null) null else Pair(x2, y2!!)
        if (x2 == null) return Pair(x1, y1!!)

        val x1m = x1.mod(p)
        val y1m = y1!!.mod(p)
        val x2m = x2.mod(p)
        val y2m = y2!!.mod(p)

        if (x1m == x2m && (y1m + y2m) % p == 0L) return null

        val lambda: Long = if (x1m == x2m && y1m == y2m) {
            if (y1m == 0L) return null
            val num = (3L * modPow(x1m, 2, p) + a.mod(p)).mod(p)
            val den = (2L * y1m).mod(p)
            if (den == 0L) return null
            num * inverseMod(den, p) % p
        } else {
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
