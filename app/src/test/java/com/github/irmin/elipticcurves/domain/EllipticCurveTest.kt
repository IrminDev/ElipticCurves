package com.github.irmin.elipticcurves.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EllipticCurveTest {

    @Test
    fun primeFieldValidation_works() {
        assertTrue(EllipticCurve(a = 2, b = 2, p = 17).isPrimeField())
        assertFalse(EllipticCurve(a = 2, b = 2, p = 15).isPrimeField())
    }

    @Test
    fun discriminant_detectsSingularCurve() {
        val singular = EllipticCurve(a = 0, b = 0, p = 17)
        assertEquals(0L, singular.discriminant())
        assertFalse(singular.isNonSingular())
    }

    @Test
    fun onCurve_andInfinity() {
        val curve = EllipticCurve(a = 2, b = 2, p = 17)
        assertTrue(curve.isOnCurve(ECPoint.Infinity))
        assertTrue(curve.isOnCurve(ECPoint.Affine(5, 1)))
        assertFalse(curve.isOnCurve(ECPoint.Affine(5, 2)))
    }

    @Test
    fun add_inverseGivesInfinity() {
        val curve = EllipticCurve(a = 2, b = 2, p = 17)
        val p = ECPoint.Affine(5, 1)
        val minusP = ECPoint.Affine(5, 16)
        assertEquals(ECPoint.Infinity, curve.add(p, minusP))
    }

    @Test
    fun multiply_matchesKnownValues() {
        val curve = EllipticCurve(a = 2, b = 2, p = 17)
        val p = ECPoint.Affine(5, 1)

        assertEquals(ECPoint.Infinity, curve.multiply(0, p))
        assertEquals(ECPoint.Affine(6, 3), curve.multiply(2, p))
        assertEquals(ECPoint.Affine(10, 6), curve.multiply(3, p))
        assertEquals(ECPoint.Infinity, curve.multiply(7, ECPoint.Infinity))
    }
}
