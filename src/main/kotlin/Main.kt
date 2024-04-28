import catcoder.base.DirectoryFilesRunner
import catcoder.base.Vector2
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.roundToInt

data class Step(
    val coordA: Vector2,
     val coordB: Vector2
) {
    val coords = listOf(coordA, coordB)
    val isDiagonal = coordA.x != coordB.x && coordA.y != coordB.y




    fun isCrossing(step: Step): Boolean {
        if (!step.isDiagonal || !this.isDiagonal) return false
        val allCoords = (this.coords + step.coords).toSet()
        val normalPoint = Vector2(allCoords.minOf { it.x }, allCoords.minOf { it.y })
        val allCoordsNormalized = allCoords.map { it.minus(normalPoint) }.toSet()
        val square = setOf(
            Vector2(0, 0),
            Vector2(0, 1),
            Vector2(1, 0),
            Vector2(1, 1),
        )
        return square == allCoordsNormalized
    }

    fun subtractAngles(angle1: Double, angle2: Double): Double {
        var result = angle1 - angle2

        return result
    }

    fun calculateAngleToReferencePoint(coord: Vector2): Double {
        val deltaAX = coord.x - coordA.x
        val deltaAY = coord.y - coordA.y
        val deltaBX = coord.x - coordB.x
        val deltaBY = coord.y - coordB.y
        return subtractAngles(Math.toDegrees(atan2(deltaAX.toDouble(), deltaAY.toDouble())), Math.toDegrees(atan2(deltaBX.toDouble(), deltaBY.toDouble())))
    }

}

data class Path(
    val coords: List<Vector2>
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

    fun add(coord: Vector2): Path {
        return Path(coords + coord)

    }

    fun isInside(coord: Vector2): Boolean {
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
    Pair('W', Vector2(0, 1)),
    Pair('S', Vector2(0, -1)),
    Pair('A', Vector2(-1, 0)),
    Pair('D', Vector2(1, 0)),
)

val reverseDirectionMap = directionMap.entries.associate { Pair(it.value, it.key) }

fun main2(args: Array<String>) {
    DirectoryFilesRunner("C:\\Users\\Lucky13\\IdeaProjects\\catcoder-base\\src\\main\\resources\\level2").forEach { reader, writer ->
        val numberLines = reader.readOne()[0].toInt()
        (1.. numberLines).forEach {
            val line = reader.readOne()[0]
            val currentCoord = Vector2(0, 0)
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

            val currentCoord = Vector2(0, 0)
            val allPathCoordsRelative = mutableListOf(currentCoord)
            pathSymbols.forEach { symbol ->
                allPathCoordsRelative.add(
                    allPathCoordsRelative.last().plus(directionMap[symbol]!!)
                )
            }

            val minX = allPathCoordsRelative.map { it.x }.min()
            val minY = allPathCoordsRelative.map { it.y }.min()
            val relativeZero = Vector2(minX, minY)

            val allPathCoordsAbsolute = allPathCoordsRelative.map {
                it.minus(relativeZero)
            }

            val lawnCoords = mutableMapOf<Vector2, Char>()
            lawnRows.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { indexSymbol, symbol ->
                    val y = lawnHeight - 1 - rowIndex
                    val x = indexSymbol
                    lawnCoords[Vector2(x, y)] = symbol
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

fun <T> toLinkList(list: List<T>): List<Pair<T, T>> {
    return (1..list.lastIndex).map {
        Pair(list[it-1], list[it])
    }

}

fun convertPathToString(path: List<Vector2>): String {
    val steps = toLinkList(path)
    return steps.map { it.second.minus(it.first) }.map { reverseDirectionMap[it]!! }.joinToString("")
}

fun printForVisualizer(lawnRows: List<String>, path: List<Vector2>) {
    println(lawnRows.joinToString("\n"))
    println(convertPathToString(path))
}

fun main(args: Array<String>) {
    DirectoryFilesRunner("D:\\Projects\\catcoder-base\\src\\main\\resources\\level4").forEach { reader, writer ->
        val numberOfLawns = reader.readOne()[0].toInt()
        (1..numberOfLawns).forEach { lawnIt ->
            val lawnSize = reader.readOne()
            val lawnWidth = lawnSize[0].toInt()
            val lawnHeight = lawnSize[1].toInt()

            val lawnRows = mutableListOf<String>()
            (1..lawnHeight).forEach { lawnRowIt ->
                lawnRows.add(reader.readOne()[0])
            }

            val lawnCoords = mutableMapOf<Vector2, Char>()
            lawnRows.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { indexSymbol, symbol ->
                    val y = lawnHeight - 1 - rowIndex
                    val x = indexSymbol
                    lawnCoords[Vector2(x, y)] = symbol
                }
            }

            val grassCoords = lawnCoords.filter { it.value == '.' }.map { it.key }.toSet()

            fun hasBubbles(path: List<Vector2>): Boolean {
                val currentCoord = path.last()
                val remainingCoords = grassCoords.minus(path.toSet())

                // splitting bubbles can only be possible if there are exactly 2 neighbors that are remaining grass fields
                val bubbleSplitIsPossible = currentCoord.neighbors.filter { remainingCoords.contains(it) }.size == 2
                if (!bubbleSplitIsPossible) return false

                // calculating actual bubbles is pretty expensive
                val bubbles: MutableSet<Set<Vector2>> = mutableSetOf()
                remainingCoords.forEach { coord ->
                    val neighboringBubbles = bubbles.filter { bubble ->
                        coord.neighbors.any { coordNeighbor -> bubble.contains(coordNeighbor) }
                    }
                    // create a new bubble
                    val bubble = mutableSetOf<Vector2>()
                    // merge all neighboring bubbles into that new bubble (means removing existing bubbles from the list)
                    neighboringBubbles.forEach {
                        bubbles.remove(it)
                        bubble.addAll(it)
                    }
                    bubble.add(coord)
                    bubbles.add(bubble)
                }

                return bubbles.size > 1
            }

            fun isGrass(coord: Vector2): Boolean {
                return grassCoords.contains(coord)
            }




            // todo: can i make the recursive findPath function to a async runnable?
            // todo: so it either returns a path right now or it just spawn new async runnables

            val foundPath: AtomicReference<List<Vector2>?> = AtomicReference(null)
            val queue: Queue<Supplier<List<Vector2>?>> = LinkedList()

            fun findPath(
                currentPath: List<Vector2>
            ): List<Vector2>? {
                if (currentPath.toSet() == grassCoords) return currentPath
                val currentSpot = currentPath.last()

                fun isValid(coord: Vector2): Boolean {
                    return !currentPath.contains(coord) && isGrass(coord) && !hasBubbles(currentPath + coord)
                }

                val neighbors = currentSpot.neighbors.sortedBy { currentPath.size < 2 || currentSpot.minus(currentPath[currentPath.lastIndex-1]) != it.minus(currentSpot) }
                neighbors.filter { isValid(it) }
                    .forEach {// todo: this was a first not null (executing only for the first neightbor that isValid
                        queue.add {
                            findPath(currentPath + it)
                        }

                    }
                return null
            }

            fun findPath(): List<Vector2>? {
                val allStartingPoints = grassCoords

                allStartingPoints.forEach {
                    queue.add{ findPath(listOf(it)) }
                }

                while (queue.isNotEmpty()) {
                    // todo: multithread here
                    val runnable = queue.poll()
                    val result = runnable.get()
                    if (result != null) return result!!
                }
                return null // todo: there is a bug where the process doesnt find a solution for something that shoud have a solution
                // todo: actually it was a bug that it was working at all, it just tried the first valid neighbor for each
                // todo: but this kind of proves that a breadth first algorith that prioritizes earlier changes in the path should work

            }



            val path = findPath()
            if (path == null) {
                println(lawnRows.joinToString("\n"))
                println("No path found")
            } else {
                println("Found path")
                writer.writeOne(convertPathToString(path))
            }
        }

    }
}

