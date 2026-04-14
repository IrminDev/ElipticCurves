package com.github.irmin.elipticcurves.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.github.irmin.elipticcurves.domain.ECPoint
import com.github.irmin.elipticcurves.domain.EllipticCurve
import com.github.irmin.elipticcurves.domain.ModArithmetic
import com.github.irmin.elipticcurves.presentation.formatting.toResultLabel
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
        if (p < 2 || !ModArithmetic.isPrime(p)) {
            _state.update { it.copy(errorMessage = "p debe ser un número primo mayor o igual a 2.") }
            return
        }

        val curve = EllipticCurve(a = a, b = b, p = p)

        if (!curve.isNonSingular()) {
            _state.update {
                it.copy(
                    errorMessage = "La curva es singular (discriminante ≡ 0 mod p). Por favor elige otros valores."
                )
            }
            return
        }

        val p1IsInf = s.p1IsInfinity
        val p2IsInf = s.p2IsInfinity

        val P: ECPoint
        val Q: ECPoint

        if (!p1IsInf) {
            val x1 = s.px1Input.trim().toLongOrNull()
            val y1 = s.py1Input.trim().toLongOrNull()
            if (x1 == null || y1 == null) {
                _state.update { it.copy(errorMessage = "Coordenadas del punto P inválidas.") }
                return
            }
            P = ECPoint.Affine(x1, y1)
            if (!curve.isOnCurve(P)) {
                _state.update { it.copy(errorMessage = "El punto P($x1, $y1) no pertenece a la curva.") }
                return
            }
        } else {
            P = ECPoint.Infinity
        }

        if (!p2IsInf) {
            val x2 = s.px2Input.trim().toLongOrNull()
            val y2 = s.py2Input.trim().toLongOrNull()
            if (x2 == null || y2 == null) {
                _state.update { it.copy(errorMessage = "Coordenadas del punto Q inválidas.") }
                return
            }
            Q = ECPoint.Affine(x2, y2)
            if (!curve.isOnCurve(Q)) {
                _state.update { it.copy(errorMessage = "El punto Q($x2, $y2) no pertenece a la curva.") }
                return
            }
        } else {
            Q = ECPoint.Infinity
        }

        val result = curve.add(P, Q)
        val resultStr = result.toResultLabel()

        _state.update { it.copy(resultPoint = resultStr, errorMessage = null, calculated = true) }
    }
}
