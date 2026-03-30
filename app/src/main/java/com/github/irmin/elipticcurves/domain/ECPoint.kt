package com.github.irmin.elipticcurves.domain

sealed interface ECPoint {
    data object Infinity : ECPoint

    data class Affine(val x: Long, val y: Long) : ECPoint
}
