import catcoder.base.DirectoryFilesRunner
import catcoder.base.Vector2
import java.util.LinkedList
import java.util.Queue
import java.util.Stack
import java.util.function.Supplier
import kotlin.system.measureTimeMillis


fun findPathForLawn() {

}

enum class LawnTile {
    GRASS, TREE;

    companion object {
        fun valueOf(char: Char): LawnTile {
            if (char == '.') {
                return GRASS
            } else {
                return TREE
            }
        }
    }
}
data class Lawn(
    val rows: List<String>,
) {
    val height = rows.size
    val width = rows.first().length
    val maxY = height-1
    val maxX = width-1
    val pointToTileMap: Map<Vector2, LawnTile> = rows.flatMapIndexed { rowIndex, row ->
        row.mapIndexed() { indexSymbol, symbol ->
            val y = height - 1 - rowIndex
            val x = indexSymbol
            Pair(Vector2(x, y), symbol.let { LawnTile.valueOf(it) })
        }
    }.associate { it }
    val pointOfTree = pointToTileMap.entries.first { it.value == LawnTile.TREE }.key
    val pointsOfGrass = pointToTileMap.entries.filter { it.value == LawnTile.GRASS }.map { it.key }.toSet()
    val pointsOfBorder = pointsOfGrass.filter { it.x == 0 || it.y == 0 || it.x == maxX || it.y == maxY }.toSet()
    val pointsOfCorner = pointsOfGrass.filter { (it.x == 0 || it.x == maxX) && (it.y == 0 || it.y == maxY) }
    val pointsNextToTree = pointOfTree.allNeighbors.intersect(pointsOfGrass)

    fun inBounds(point: Vector2): Boolean {
        if (point.x < 0 || point.y < 0) return false
        if (point.x > maxX || point.y > maxY) return false
        return true
    }

    fun print() {
        rows.joinToString("\n").let { println(it) }
    }
}

enum class Spin(val rotationValue: Int) { // rotationValue is given in units of 90 degree turns, so possible values is -1, 0, 1 (2 would be a 180 degree turn)
    LEFT(-1), STRAIGHT(0), RIGHT(1);

    fun applyTo(direction: Vector2): Vector2 {
        return Vector2.Directions.rotate(direction, rotationValue)
    }
}

interface IterativeResult {
    fun getNext(): IterativeResult
}

class ConclusiveResult(
    val result: List<Vector2>?
): IterativeResult {
    override fun getNext(): IterativeResult {
        throw Exception()
    }
}



abstract class AbstractNode(
    val lawn: Lawn
) {
    val queue: Queue<Supplier<AbstractNode>> = LinkedList()
    var terminated = false

    open fun getPath(): IterativeResult {
        if (terminated) {
            println("WARN: do not call a terminated node")
            return ConclusiveResult(null)
        }
        if (queue.isEmpty()) {
            terminated = true
            return ConclusiveResult(null)
        }
        return object: IterativeResult{
            override fun getNext(): IterativeResult {
                val childNode = queue.poll().get()
                val result = childNode.getPath()
                if (!childNode.terminated) {
                    queue.add{ childNode }
                }
                return result
            }

        }

    }
}

class RootNode(
    lawn: Lawn
): AbstractNode(lawn) {
    init {
        val startingPoints = (lawn.pointsOfCorner + lawn.pointsNextToTree).toSet() // todo: maybe just corners and coords in between tree and border
        startingPoints.forEach { startingPoint ->
            queue.add {
                StartNode(lawn, startingPoint)
            }
        }
    }
}

class StartNode(
    lawn: Lawn,
    val startPoint: Vector2,
): AbstractNode(lawn) {
    init {
        Vector2.Directions.CARDINAL.forEach { direction ->
            val nextPoint = startPoint.plus(direction)
            val hitsTree = {
                nextPoint == lawn.pointOfTree
            }
            val leavesField = {
                !lawn.inBounds(nextPoint)
            }

            if (!hitsTree() && !leavesField()) {
                queue.add {
                    val path = listOf(startPoint, nextPoint)
                    RegularNode(lawn, path, direction, Spin.STRAIGHT)
                }
            }
        }
    }
}

fun hasBubbles(lawn: Lawn, path: List<Vector2>): Boolean {
    val currentCoord = path.last()
    val remainingCoords = lawn.pointsOfGrass.minus(path.toSet())

    // splitting bubbles can only be possible if there are exactly 2 neighbors that are remaining grass fields
    val neighborsThatAreStillOpen = currentCoord.neighbors.filter { remainingCoords.contains(it) }
    val hasExactlyTwoValidNeighbors = neighborsThatAreStillOpen.size == 2
    if (!hasExactlyTwoValidNeighbors) return false

    // make cheap check if the two neighbors are directly connected first
    val firstNeighbor = neighborsThatAreStillOpen.first()
    val secondNeighbor = neighborsThatAreStillOpen[1]
    val neighborsAreDiagonalNeighbors = firstNeighbor.x != secondNeighbor.x && firstNeighbor.y != secondNeighbor.y
    if (neighborsAreDiagonalNeighbors) {
        val possibleConnections = listOf(Vector2(firstNeighbor.x, secondNeighbor.y), Vector2(secondNeighbor.x, firstNeighbor.y))
        val neighborsAreConnected = possibleConnections.any { remainingCoords.contains(it) }
        if (neighborsAreConnected) return false
    }

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

fun <T> toLinkList(list: List<T>): List<Pair<T, T>> {
    return (1..list.lastIndex).map {
        Pair(list[it-1], list[it])
    }
}
val directionMap = mapOf(
    Pair('W', Vector2(0, 1)),
    Pair('S', Vector2(0, -1)),
    Pair('A', Vector2(-1, 0)),
    Pair('D', Vector2(1, 0)),
)

val reverseDirectionMap = directionMap.entries.associate { Pair(it.value, it.key) }

fun convertPathToString(path: List<Vector2>): String {
    val steps = toLinkList(path)
    val firstCoord = path.first()
    return "${firstCoord} " + steps.map { it.second.minus(it.first) }.map { reverseDirectionMap[it]!! }.joinToString("")
}

class RegularNode(
    lawn: Lawn,
    val currentPath: List<Vector2>,
    val direction: Vector2,
    val spin: Spin
): AbstractNode(lawn) {
    val sortedSpins: List<Spin> = run {
        val spins: MutableList<Spin> = mutableListOf()
        if (spin != Spin.STRAIGHT) {
            spins.add(spin)
        }
        spins.add(Spin.STRAIGHT)
        if (spin != Spin.RIGHT) spins.add(Spin.RIGHT)
        if (spin != Spin.LEFT) spins.add(Spin.LEFT)
        spins.toList()
    }
    val innerQueue: Queue<Spin> = LinkedList(sortedSpins)
    var finishedPath: List<Vector2>? = null

    override fun getPath(): IterativeResult { // todo: can this work without recursion.... when the fields get too big the stack overflows
        if (finishedPath != null) return ConclusiveResult(finishedPath)
        while(innerQueue.isNotEmpty()) {
            val spinToValidate = innerQueue.poll()
            val direction = if (spinToValidate == Spin.STRAIGHT) this.direction else this.direction.let { spinToValidate.applyTo(it) }
            val nextPoint = currentPath.last().plus(direction)
            val hitsTree = {
                nextPoint == lawn.pointOfTree
            }
            val leavesField = {
                !lawn.inBounds(nextPoint)
            }
            val crossesItself = {
                currentPath.toSet().contains(nextPoint)
            }
            val createsBubbles = {
                hasBubbles(lawn, currentPath + nextPoint)
            }
            if (
                !hitsTree() &&
                !leavesField() &&
                !crossesItself() &&
                !createsBubbles()
            ) {
                if (lawn.pointsOfGrass.size == (currentPath + 1).size) {
                    return ConclusiveResult(currentPath + nextPoint)
                } else {
                    return object: IterativeResult {
                        override fun getNext(): IterativeResult {
                            val path = currentPath + nextPoint
                            val newNode = RegularNode(lawn, path, direction, spinToValidate)
                            assert(!newNode.terminated)

                            val result = newNode.getPath()
                            if ((result !is ConclusiveResult || result.result == null) && !newNode.terminated) {
                                queue.add{ newNode }
                            }
                            return result
                        }

                    }


                }

            }
        }
        return super.getPath()
    }
}

fun findPath(lawn: Lawn) {

}

fun main(args: Array<String>) {
    DirectoryFilesRunner("D:\\Projects\\catcoder-base\\src\\main\\resources\\level4").forEach { reader, writer ->
        println("Processing ${reader.file.name}")
        val numberOfLawns = reader.readOne()[0].toInt()
        (1..numberOfLawns).forEach { lawnIt ->

            val lawnSize = reader.readOne()
            val lawnWidth = lawnSize[0].toInt()
            val lawnHeight = lawnSize[1].toInt()

            println("Processing lawn ${lawnIt}/${numberOfLawns} (${lawnWidth}x${lawnHeight})")

            val lawnRows = mutableListOf<String>()
            (1..lawnHeight).forEach { lawnRowIt ->
                lawnRows.add(reader.readOne()[0])
            }

            val lawn = Lawn(lawnRows)
            lawn.print()

            val rootNode = RootNode(lawn)
            var path: List<Vector2>? = null
            val milis = measureTimeMillis {
                while (path == null && !rootNode.terminated ) {
                    var result: IterativeResult
                    result = rootNode.getPath()
                    while (result !is ConclusiveResult) {
                        result = result.getNext()
                    }
                    path = result.result
                }
            }

            println("Found path in ${milis} miliseconds.")
            if (path == null) throw Exception("there is no way there is no way")
            val representation = path!!.let {convertPathToString(it)}
            println(representation)
            writer.writeOne(representation)
        }

    }
}


