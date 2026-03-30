package com.github.irmin.elipticcurves.domain

/**
 * Short Weierstrass curve over a prime field F_p:
 *   y^2 = x^3 + ax + b  (mod p)
 */
data class EllipticCurve(
    val a: Long,
    val b: Long,
    val p: Long
) {
    fun isPrimeField(): Boolean = p >= 2L && ModArithmetic.isPrime(p)

    /** 4a^3 + 27b^2 (mod p). Curve is non-singular iff discriminant != 0. */
    fun discriminant(): Long {
        val term1 = 4L * ModArithmetic.modPow(a, 3, p)
        val term2 = 27L * ModArithmetic.modMul(b, b, p)
        return ModArithmetic.mod(term1 + term2, p)
    }

    fun isNonSingular(): Boolean = discriminant() != 0L

    fun rhs(x: Long): Long {
        val xm = ModArithmetic.mod(x, p)
        val x3 = ModArithmetic.modPow(xm, 3, p)
        val ax = ModArithmetic.modMul(a, xm, p)
        return ModArithmetic.mod(x3 + ax + b, p)
    }

    fun isOnCurve(point: ECPoint): Boolean {
        return when (point) {
            ECPoint.Infinity -> true
            is ECPoint.Affine -> {
                val x = ModArithmetic.mod(point.x, p)
                val y = ModArithmetic.mod(point.y, p)
                val lhs = ModArithmetic.modPow(y, 2, p)
                val rhs = rhs(x)
                lhs == rhs
            }
        }
    }

    /**
     * Group law: returns P + Q.
     */
    fun add(p1: ECPoint, p2: ECPoint): ECPoint {
        if (p1 === ECPoint.Infinity) return p2
        if (p2 === ECPoint.Infinity) return p1

        val P = p1 as ECPoint.Affine
        val Q = p2 as ECPoint.Affine

        val x1 = ModArithmetic.mod(P.x, p)
        val y1 = ModArithmetic.mod(P.y, p)
        val x2 = ModArithmetic.mod(Q.x, p)
        val y2 = ModArithmetic.mod(Q.y, p)

        // P = -Q
        if (x1 == x2 && ModArithmetic.mod(y1 + y2, p) == 0L) return ECPoint.Infinity

        val lambda = if (x1 == x2 && y1 == y2) {
            // Doubling
            if (y1 == 0L) return ECPoint.Infinity
            val num = ModArithmetic.mod(3L * ModArithmetic.modPow(x1, 2, p) + a, p)
            val den = ModArithmetic.mod(2L * y1, p)
            if (den == 0L) return ECPoint.Infinity
            ModArithmetic.mod(num * ModArithmetic.modInverse(den, p), p)
        } else {
            // Addition
            val num = ModArithmetic.mod(y2 - y1, p)
            val den = ModArithmetic.mod(x2 - x1, p)
            if (den == 0L) return ECPoint.Infinity
            ModArithmetic.mod(num * ModArithmetic.modInverse(den, p), p)
        }

        val x3 = ModArithmetic.mod(ModArithmetic.modPow(lambda, 2, p) - x1 - x2, p)
        val y3 = ModArithmetic.mod(lambda * (x1 - x3) - y1, p)

        return ECPoint.Affine(x3, y3)
    }

    /**
     * Scalar multiplication kP with double-and-add.
     */
    fun multiply(k: Long, point: ECPoint): ECPoint {
        require(k >= 0L) { "k must be non-negative" }
        if (k == 0L) return ECPoint.Infinity
        if (point === ECPoint.Infinity) return ECPoint.Infinity

        var result: ECPoint = ECPoint.Infinity
        var addend: ECPoint = point
        var kk = k

        while (kk > 0L) {
            if ((kk and 1L) == 1L) result = add(result, addend)
            addend = add(addend, addend)
            kk = kk shr 1
        }

        return result
    }
}
