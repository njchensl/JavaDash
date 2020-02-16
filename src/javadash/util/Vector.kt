package javadash.util

import kotlin.math.acos
import kotlin.math.sqrt

data class Vector(val x: Double = 0.0, val y: Double = 0.0) {

    constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())

    val magnitude: Double
        get() = sqrt(this dot this)

    fun getUnitVector(): Vector {
        if (magnitude == 0.0) {
            throw ArithmeticException("A 0 vector has no direction")
        }
        return Vector(x / magnitude, y / magnitude)
    }

    fun add(that: Vector): Vector {
        return Vector(x + that.x, y + that.y)
    }

    operator fun plus(that: Vector): Vector = add(that)

    fun subtract(that: Vector): Vector {
        return Vector(x - that.x, y - that.y)
    }

    operator fun minus(that: Vector): Vector = subtract(that)

    infix fun dot(that: Vector): Double {
        return x * that.x + y * that.y
    }

    fun multiply(x: Double): Vector {
        return Vector(x * this.x, x * y)
    }

    operator fun times(x: Number): Vector = multiply(x.toDouble())

    fun getCartesianForm(): String = "[$x, $y]"

    fun isScalarMultipleOf(that: Vector): Boolean {
        val multiple = that.x / x
        return y * multiple == that.y
    }

    fun projectOnto(that: Vector): Vector {
        return that.multiply(dot(that) / that.dot(that))
    }

    override fun toString(): String {
        return getCartesianForm()
    }

    companion object {
        @JvmStatic
        fun getAngle(u: Vector, v: Vector): Double {
            return acos((u dot v) / (u.magnitude * v.magnitude))
        }
    }
}