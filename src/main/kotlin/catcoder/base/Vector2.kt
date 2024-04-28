package catcoder.base

import kotlin.math.absoluteValue

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
}