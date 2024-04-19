import catcoder.base.DirectoryFilesRunner
import com.sun.org.apache.xpath.internal.operations.Bool
import kotlin.math.log

data class Cell(val x: Int, val y: Int)

fun isNeighbor(cell1: Coord, cell2: Coord): Boolean {
    val dx = Math.abs(cell1.x - cell2.x)
    val dy = Math.abs(cell1.y - cell2.y)
    return (dx == 1 && dy == 0) || (dx == 0 && dy == 1)
}

fun getNeighbors(cell: Coord, lawnWidth: Int, lawnHeight: Int): List<Coord> {
    val neighbors = mutableListOf<Coord>()
    val dx = arrayOf(-1, 0, 1, 0) // Left, Down, Right, Up
    val dy = arrayOf(0, 1, 0, -1)
    for (i in 0..3) {
        val newX = cell.x + dx[i]
        val newY = cell.y + dy[i]
        // Check if within bounds and not a diagonal move
        if (newX >= 0 && newX < lawnWidth && newY >= 0 && newY < lawnHeight && (dx[i] != 0 || dy[i] != 0)) {
            neighbors.add(Coord(newX, newY))
        }
    }
    return neighbors
}

fun isVisitedAll(visitedCells: Set<Coord>, lawnWidth: Int, lawnHeight: Int): Boolean {
    return visitedCells.size == lawnWidth * lawnHeight
}

fun isValidPath(lawn: Array<CharArray>, path: List<Coord>, treePosition: Coord): Boolean {
    if (path.isEmpty()) {
        return false
    }
    val visitedCells = mutableSetOf<Coord>()
    for (cell in path) {
        if (cell == treePosition) {
            return false
        }
        if (visitedCells.contains(cell)) {
            return false
        }
        visitedCells.add(cell)
    }
    return isVisitedAll(visitedCells, lawn.size, lawn[0].size)
}

fun printCoordListAsSymbols(path: List<Coord>): String {
    val symbolReverseMap = directionMap.entries.associate { Pair(it.value, it.key) }
    var pathAsSymbols = ""
    (1..path!!.lastIndex).forEach {
        val from = path[it-1]
        val to = path[it]

        val diff = to.minus(from)
        pathAsSymbols += symbolReverseMap[diff]!!
    }
    return pathAsSymbols
}



fun findPath(
    lawn: Array<CharArray>,
    currentCell: Coord,
    visitedCells: Set<Coord>,
    path: List<Coord>,
    treePosition: Coord
): List<Coord>? {
    if (isVisitedAll(visitedCells, lawn[0].size, lawn.size) && isValidPath(lawn, path, treePosition)) {
        return path
    }
    printCoordListAsSymbols(path).let { println(it) }
    val neighbors = getNeighbors(currentCell, lawn[0].size, lawn.size)
    for (neighbor in neighbors) {
        if(neighbor == treePosition){
            continue
        }
        if (!visitedCells.contains(neighbor)) {
            val newVisitedCells = visitedCells + neighbor
            val newPath = path + currentCell
            val result = findPath(lawn, neighbor, newVisitedCells, newPath, treePosition)
            if (result != null) {
                return result
            }
        }
    }
    return null
}

fun main() {
    DirectoryFilesRunner("C:\\Users\\Lucky13\\IdeaProjects\\catcoder-base\\src\\main\\resources\\bonus").forEach { reader, writer ->
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

            val allDrivableLawnCoords = lawnCoords.entries.filter { it.value != 'X' }.map { it.key }.toSet()

            fun isTree(coord: Coord): Boolean {
                return lawnCoords.get(coord)!! == 'X'
            }

            val treePosition = lawnCoords.filter { isTree(it.key) }.toList()[0].first// Find the tree position in the lawn (replace with logic to find tree)

            // Choose a starting cell (e.g., top-left corner)
            val startCell = Coord(0g, lawnHeight -1)

            val visitedCells = emptySet<Coord>()
            val path = emptyList<Coord>()
            val foundPath = findPath(lawnRows.map { it.toCharArray() }.toTypedArray(), startCell, visitedCells, path, treePosition)

            if (foundPath != null) {
                println("Path found: $foundPath")
                // You can visualize the path here using visualizer.html (if provided)
            } else {
                println("No path found for this lawn")
            }


        }

    }
}

fun main5(args: Array<String>) {
    data class TreeDistance(
        val south: Int,
        val north: Int,
        val west: Int,
        val east: Int,
    )
    val treeDistances = mutableListOf<TreeDistance>()
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

            val treeCoord = lawnCoords.entries.filter { it.value == 'X' }.get(0).key

            treeDistances.add(
                TreeDistance(
                    north = lawnHeight - treeCoord.y - 1,
                    west = treeCoord.y,
                    east = lawnWidth - treeCoord.x - 1,
                    south = treeCoord.x
                )
            )
        }

    }

    data class TreeCase(
        val northEven: Boolean,
        val eastEven: Boolean,
        val southEven: Boolean,
        val westEvent: Boolean,
    )
    treeDistances.map {
        TreeCase(
            it.north % 2 == 0,
            it.east % 2 == 0,
            it.south % 2 == 0,
            it.west % 2 == 0,
        )
    }.toSet().let { println(it.contains(TreeCase(true, false, true, false))) }
}

