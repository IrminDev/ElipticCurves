package com.github.irmin.elipticcurves.domain

/**
 * Enumerates the points of the curve E(F_p) by brute force.
 *
 * Ordering is deterministic and matches the UI tables used across the app:
 * 1) Infinity (O)
 * 2) Affine points in increasing x, then increasing y (0..p-1)
 */
fun EllipticCurve.enumeratePoints(): List<ECPoint> {
    val prime = p
    require(prime >= 2L) { "p must be >= 2" }

    return buildList {
        add(ECPoint.Infinity)
        for (x in 0 until prime.toInt()) {
            val ex = rhs(x.toLong()).toInt()
            for (y in 0 until prime.toInt()) {
                val ySquaredMod = ModArithmetic.modPow(y.toLong(), 2, prime).toInt()
                if (ySquaredMod == ex) {
                    add(ECPoint.Affine(x.toLong(), y.toLong()))
                }
            }
        }
    }
}

/**
 * Returns |E(F_p)| including the point at infinity (O).
 */
fun EllipticCurve.curveOrder(): Int = enumeratePoints().size
