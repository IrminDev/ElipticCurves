package com.github.irmin.elipticcurves.domain

import kotlin.math.sqrt

internal object ModArithmetic {
    fun mod(value: Long, mod: Long): Long {
        val r = value % mod
        return if (r >= 0) r else r + mod
    }

    fun modMul(a: Long, b: Long, mod: Long): Long {
        return (mod(a, mod) * mod(b, mod)) % mod
    }

    fun modPow(base: Long, exp: Long, mod: Long): Long {
        var result = 1L
        var b = mod(base, mod)
        var e = exp
        while (e > 0) {
            if ((e and 1L) == 1L) result = (result * b) % mod
            b = (b * b) % mod
            e = e shr 1
        }
        return result
    }

    /**
     * Simple primality test (sufficient for small p).
     */
    fun isPrime(n: Long): Boolean {
        if (n < 2) return false
        if (n == 2L) return true
        if (n % 2L == 0L) return false
        val limit = sqrt(n.toDouble()).toLong()
        var i = 3L
        while (i <= limit) {
            if (n % i == 0L) return false
            i += 2L
        }
        return true
    }

    /**
     * Modular inverse using extended Euclidean algorithm.
     *
     * Requires gcd(a, p) == 1.
     */
    fun modInverse(a: Long, p: Long): Long {
        val am = mod(a, p)
        require(p > 1) { "p must be > 1" }
        require(am != 0L) { "0 has no modular inverse mod p" }

        var t = 0L
        var newT = 1L
        var r = p
        var newR = am

        while (newR != 0L) {
            val q = r / newR

            val tmpT = t
            t = newT
            newT = tmpT - q * newT

            val tmpR = r
            r = newR
            newR = tmpR - q * newR
        }

        require(r == 1L) { "a is not invertible mod p" }

        if (t < 0L) t += p
        return t
    }
}
