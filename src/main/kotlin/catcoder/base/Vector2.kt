package catcoder.base

import kotlin.math.absoluteValue
import kotlin.math.acos
import kotlin.math.sqrt

data class Vector2(
    val x: Int,
    val y: Int
) {
    class Directions {
        companion object {
            val UP = Vector2(0, 1)
            val DOWN = Vector2(0, -1)
            val LEFT = Vector2(-1, 0)
            val RIGHT = Vector2(1, 0)

            val UP_LEFT = UP.plus(LEFT)
            val UP_RIGHT = UP.plus(RIGHT)
            val DOWN_LEFT = DOWN.plus(LEFT)
            val DOWN_RIGHT = DOWN.plus(RIGHT)

            val NORTH = UP
            val EAST = RIGHT
            val SOUTH = DOWN
            val WEST = LEFT

            val NORTH_EAST = UP_RIGHT
            val SOUTH_EAST = DOWN_RIGHT
            val NORTH_WEST = UP_LEFT
            val SOUTH_WEST = DOWN_LEFT

            val CARDINAL = setOf(
                RIGHT,
                UP,
                DOWN,
                LEFT
            )
            val INTERCARDINAL = setOf(
                UP_LEFT,
                UP_RIGHT,
                DOWN_LEFT,
                DOWN_RIGHT
            )
            val ALL = CARDINAL + INTERCARDINAL

            val ROTATION = listOf(UP, RIGHT, DOWN, LEFT)

            fun angleBetween(directionA: Vector2, directionB: Vector2): Int { // unit is amount of 90 degrees
                val aIndex = ROTATION.indexOf(directionA) // 3
                val bIndex = ROTATION.indexOf(directionB) // 0

                val diff = bIndex - aIndex

                return diff.let {
                    if (it.absoluteValue == 3) { // if three turns you can also do a rotation of one in the other direction
                        (it / -3)
                    } else {
                        it
                    }
                }
            }

            fun rotate(direction: Vector2, rotation: Int): Vector2 {
                if (rotation == 0) {
                    println("No rotation is necessary with 0.")
                    return direction
                }
                val index = ROTATION.indexOf(direction)
                val normalizedIndex = (index + rotation).let {
                    var normalized = it
                    while (normalized < 0) {
                        normalized = ROTATION.lastIndex + normalized
                    }
                    normalized
                }.let {
                    it % ROTATION.size
                }

                return ROTATION[normalizedIndex]
            }

        }
    }

    val inverse: Vector2
        get() = Vector2(-x, -y)

    val isHorizontal: Boolean
        get() = x != 0

    val isVertical: Boolean
        get() = y != 0

    val neighbors: List<Vector2>
        get() = cardinalNeighbors
    val cardinalNeighbors: List<Vector2>
        get() = Directions.CARDINAL.map { this.plus(it) }

    val intercardinalNeighbors: List<Vector2>
        get() = Directions.INTERCARDINAL.map { this.plus(it) }

    val allNeighbors: List<Vector2>
        get() = cardinalNeighbors + intercardinalNeighbors

    fun minus(vector2: Vector2): Vector2 {
        return Vector2(this.x - vector2.x, this.y - vector2.y)
    }

    fun plus(vector2: Vector2): Vector2 {
        return Vector2(this.x + vector2.x, this.y + vector2.y)
    }

    fun product(vector2: Vector2): Int {
        val a = this
        val b = vector2

        return (a.x * b.x) + (a.y * b.y)
    }

    fun magnitude(): Double {
        return sqrt(((this.x * this.x) + (this.y * this.y)).toDouble())
    }

    fun angleTo(vector2: Vector2): Double {
        val a = this
        val b = vector2

        val product = a.product(b)
        val cosAngle = product / (a.magnitude() * b.magnitude())
        return acos(cosAngle) * (180 / Math.PI)
    }
}