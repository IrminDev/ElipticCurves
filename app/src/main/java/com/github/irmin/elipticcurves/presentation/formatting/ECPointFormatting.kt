package com.github.irmin.elipticcurves.presentation.formatting

import com.github.irmin.elipticcurves.domain.ECPoint

internal fun ECPoint.toResultLabel(): String {
    return when (this) {
        ECPoint.Infinity -> "O (Punto al infinito)"
        is ECPoint.Affine -> "(${this.x}, ${this.y})"
    }
}

internal fun ECPoint.toCompactLabel(): String {
    return when (this) {
        ECPoint.Infinity -> "O"
        is ECPoint.Affine -> "(${this.x}, ${this.y})"
    }
}
