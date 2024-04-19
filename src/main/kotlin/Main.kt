import catcoder.base.DirectoryFilesRunner
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.roundToInt

data class Coord(
    val x: Int,
    val y: Int
) {
    fun getConnectedCoords(): List<Coord> {
        return listOf(
            Coord(x+1, y),
            Coord(x-1, y),
            Coord(x, y+1),
            Coord(x, y-1)
        )
    }

    fun getConnectedCoordsWithDiagonal(): List<Coord> {
        return listOf(
            Coord(x+1, y),
            Coord(x-1, y),
            Coord(x, y+1),
            Coord(x, y-1),
            Coord(x-1, y-1),
            Coord(x+1, y-1),
            Coord(x+1, y+1),
            Coord(x-1, y+1),
        )
    }

    fun minus(coord: Coord): Coord {
        return Coord(this.x - coord.x, this.y - coord.y)
    }

    fun plus(coord: Coord): Coord {
        return Coord(this.x + coord.x, this.y + coord.y)
    }
}

data class Step(
    val coordA: Coord,
     val coordB: Coord
) {
    val coords = listOf(coordA, coordB)
    val isDiagonal = coordA.x != coordB.x && coordA.y != coordB.y




    fun isCrossing(step: Step): Boolean {
        if (!step.isDiagonal || !this.isDiagonal) return false
        val allCoords = (this.coords + step.coords).toSet()
        val normalPoint = Coord(allCoords.minOf { it.x }, allCoords.minOf { it.y })
        val allCoordsNormalized = allCoords.map { it.minus(normalPoint) }.toSet()
        val square = setOf(
            Coord(0, 0),
            Coord(0, 1),
            Coord(1, 0),
            Coord(1, 1),
        )
        return square == allCoordsNormalized
    }

    fun subtractAngles(angle1: Double, angle2: Double): Double {
        var result = angle1 - angle2

        return result
    }

    fun calculateAngleToReferencePoint(coord: Coord): Double {
        val deltaAX = coord.x - coordA.x
        val deltaAY = coord.y - coordA.y
        val deltaBX = coord.x - coordB.x
        val deltaBY = coord.y - coordB.y
        return subtractAngles(Math.toDegrees(atan2(deltaAX.toDouble(), deltaAY.toDouble())), Math.toDegrees(atan2(deltaBX.toDouble(), deltaBY.toDouble())))
    }

}

data class Path(
    val coords: List<Coord>
) {
    val size = coords.size
    val steps = (0..<(coords.size-1)).map {
        Step(coords[it], coords[it + 1])
    }
    val loopSteps = steps + Step(coords.last(), coords.first())
    fun isTileVisitedTwice(): Boolean {
        return coords.groupBy { it }.any { it.value.size > 1 }
    }

    fun isAnyCrossing(): Boolean {
        (0..<steps.size-1).forEach { i ->
            (i+1..<steps.size).forEach { j ->
                if (steps[i].isCrossing(steps[j])) return true
            }
        }
        return false
    }

    fun isValid (): Boolean {
        return !isTileVisitedTwice() && !isAnyCrossing()
    }

    fun add(coord: Coord): Path {
        return Path(coords + coord)

    }

    fun isInside(coord: Coord): Boolean {
        val angleTotal = loopSteps.sumOf { it.calculateAngleToReferencePoint(coord) }
        return angleTotal.roundToInt().absoluteValue % 360 == 0
    }


}

fun main1(args: Array<String>) {
    DirectoryFilesRunner("C:\\Users\\Lucky13\\IdeaProjects\\catcoder-base\\src\\main\\resources\\level1").forEach { reader, writer ->
        val numberLines = reader.readOne()[0].toInt()
        (1.. numberLines).forEach {
            val line = reader.readOne()[0]
            val map = line.groupBy { it }.mapValues { it.value.size }
            val counts = listOf(
                map['W'] ?: 0,
                map['D'] ?: 0,
                map['S'] ?: 0,
                map['A'] ?: 0,
            )
           val result = counts.joinToString(" ")
            println(result)
            writer.writeOne(result)
        }

    }
}

val directionMap = mapOf(
    Pair('W', Coord(0, 1)),
    Pair('S', Coord(0, -1)),
    Pair('A', Coord(-1, 0)),
    Pair('D', Coord(1, 0)),
)

fun main2(args: Array<String>) {
    DirectoryFilesRunner("C:\\Users\\Lucky13\\IdeaProjects\\catcoder-base\\src\\main\\resources\\level2").forEach { reader, writer ->
        val numberLines = reader.readOne()[0].toInt()
        (1.. numberLines).forEach {
            val line = reader.readOne()[0]
            val currentCoord = Coord(0, 0)
            val allCoords = mutableListOf(currentCoord)
            line.forEach { symbol ->
                allCoords.add(
                    allCoords.last().plus(directionMap[symbol]!!)
                )
            }
            val maxX = allCoords.map { it.x }.max()
            val minX = allCoords.map { it.x }.min()
            val maxY = allCoords.map { it.y }.max()
            val minY = allCoords.map { it.y }.min()

            val absX = maxX-minX + 1
            val absY = maxY-minY + 1

            val result = "$absX $absY"
            println(result)
            writer.writeOne(result)


        }

    }
}

fun main3(args: Array<String>) {
    DirectoryFilesRunner("C:\\Users\\Lucky13\\IdeaProjects\\catcoder-base\\src\\main\\resources\\level3").forEach { reader, writer ->
        val numberOfLawns = reader.readOne()[0].toInt()
        (1..numberOfLawns).forEach { lawnIt ->
            val lawnSize = reader.readOne()
            val lawnWidth = lawnSize[0].toInt()
            val lawnHeight = lawnSize[1].toInt()

            val lawnRows = mutableListOf<String>()
            (1..lawnHeight).forEach { lawnRowIt ->
                lawnRows.add(reader.readOne()[0])
            }
            val pathSymbols = reader.readOne()[0]

            val currentCoord = Coord(0, 0)
            val allPathCoordsRelative = mutableListOf(currentCoord)
            pathSymbols.forEach { symbol ->
                allPathCoordsRelative.add(
                    allPathCoordsRelative.last().plus(directionMap[symbol]!!)
                )
            }

            val minX = allPathCoordsRelative.map { it.x }.min()
            val minY = allPathCoordsRelative.map { it.y }.min()
            val relativeZero = Coord(minX, minY)

            val allPathCoordsAbsolute = allPathCoordsRelative.map {
                it.minus(relativeZero)
            }

            val lawnCoords = mutableMapOf<Coord, Char>()
            lawnRows.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { indexSymbol, symbol ->
                    val y = lawnHeight - 1 - rowIndex
                    val x = indexSymbol
                    lawnCoords[Coord(x, y)] = symbol
                }
            }

            val allDrivableLawnCoords = lawnCoords.entries.filter { it.value != 'X' }.map { it.key }.toSet()

            val allDrivableLawnCoordsHaveBeenEntered = allDrivableLawnCoords == allPathCoordsAbsolute.toSet()
            val noCrossings = allPathCoordsAbsolute.size == allPathCoordsAbsolute.toSet().size

            val result = if (allDrivableLawnCoordsHaveBeenEntered && noCrossings) "VALID" else "INVALID"
            println(result)

            writer.writeOne(result)
        }

    }
}

data class TreeDistance(
    val south: Int,
    val north: Int,
    val west: Int,
    val east: Int,
) {
    companion object {
        fun ofLawnCoords(lawnCoords: Map<Coord, Char>): TreeDistance {
            val treeCoord = lawnCoords.entries.filter { it.value == 'X' }.get(0).key

            return  TreeDistance(
                north = lawnCoords.map { it.key.y }.max() - treeCoord.y,
                west = treeCoord.y,
                east = lawnCoords.map { it.key.x}.max() - treeCoord.x ,
                south = treeCoord.x % 2
            )
        }
    }
}

fun main7(args: Array<String>) {
    DirectoryFilesRunner("C:\\Users\\Lucky13\\IdeaProjects\\catcoder-base\\src\\main\\resources\\level4").forEach { reader, writer ->
        val numberOfLawns = reader.readOne()[0].toInt()
        (1..numberOfLawns).forEach { lawnIt ->
            val lawnSize = reader.readOne()
            val lawnWidth = lawnSize[0].toInt()
            val lawnHeight = lawnSize[1].toInt()

            val lawnRows = mutableListOf<String>()
            (1..lawnHeight).forEach { lawnRowIt ->
                lawnRows.add(reader.readOne()[0])
            }

            val lawnCoords = mutableMapOf<Coord, Char>()
            lawnRows.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { indexSymbol, symbol ->
                    val y = lawnHeight - 1 - rowIndex
                    val x = indexSymbol
                    lawnCoords[Coord(x, y)] = symbol
                }
            }

            val treeDistance = TreeDistance.ofLawnCoords(lawnCoords)

            // todo: break it down to thomsche cases


        }

    }
}

