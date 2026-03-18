package com.github.irmin.elipticcurves

import androidx.lifecycle.ViewModel

class PointsCalculationViewModel : ViewModel() {
    fun inverseMod(a: Long, p: Long): Long {
        var a0 = a
        var p0 = p
        var q = (p0 / a0).toLong()
        var r = (p0 % a0)
        var t = 0L
        var t0 = 1L
        while(r != 0L) {
            a0 = p0
            p0 = r
            val temp = t
            t = t0 - (q * t)
            t0 = temp

            q = (a0 / p0).toLong()
            r = (a0 % p0)
        }
        if (t0 < 0) {
            t0 += p
        }
        return t0
    }
}