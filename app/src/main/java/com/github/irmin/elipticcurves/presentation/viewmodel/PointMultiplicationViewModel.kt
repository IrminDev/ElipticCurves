package com.github.irmin.elipticcurves.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.github.irmin.elipticcurves.domain.ECPoint
import com.github.irmin.elipticcurves.domain.EllipticCurve
import com.github.irmin.elipticcurves.domain.ModArithmetic
import com.github.irmin.elipticcurves.domain.curveOrder
import com.github.irmin.elipticcurves.presentation.formatting.toResultLabel
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
        if (p < 2 || !ModArithmetic.isPrime(p)) {
            _state.update { it.copy(errorMessage = "p debe ser un número primo mayor o igual a 2.") }
            return
        }
        if (k < 0) {
            _state.update { it.copy(errorMessage = "k debe ser un entero no negativo.") }
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

        val curveOrder = curve.curveOrder()
        if (curveOrder <= 1) {
            _state.update { it.copy(errorMessage = "No se pudo determinar un conjunto de puntos válido para la curva.") }
            return
        }
        if (k <= 0L || k >= curveOrder.toLong()) {
            _state.update {
                it.copy(
                    errorMessage = "k debe estar en el rango 1 ≤ k < |E|. Para esta curva, |E| = $curveOrder."
                )
            }
            return
        }

        val pIsInf = s.pIsInfinity
        val P: ECPoint = if (!pIsInf) {
            val x = s.pxInput.trim().toLongOrNull()
            val y = s.pyInput.trim().toLongOrNull()
            if (x == null || y == null) {
                _state.update { it.copy(errorMessage = "Coordenadas del punto P inválidas.") }
                return
            }
            ECPoint.Affine(x, y).also { point ->
                if (!curve.isOnCurve(point)) {
                    _state.update { it.copy(errorMessage = "El punto P($x, $y) no pertenece a la curva.") }
                    return
                }
            }
        } else {
            ECPoint.Infinity
        }

        val result = curve.multiply(k, P)
        val resultStr = result.toResultLabel()

        _state.update { it.copy(resultPoint = resultStr, errorMessage = null, calculated = true) }
    }
}
