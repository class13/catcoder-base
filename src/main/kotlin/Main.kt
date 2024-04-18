import catcoder.base.DirectoryFilesRunner
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt

fun main1(args: Array<String>) {
    DirectoryFilesRunner("C:\\Users\\Lucky13\\IdeaProjects\\catcoder-base\\src\\main\\resources\\level1").forEach { reader, writer ->
        val amountOfRows = reader.readOne()[0].toInt()
        val rows = (0..<amountOfRows).map {
            reader.readOne()[0]
        }
        val amountCoordinates = reader.readOne()[0].toInt()
        val coordinates = (0..<amountCoordinates).map {
            reader.readOne(",")
        }

        val symbolAtCoords = coordinates.map {
            rows[it[1].toInt()][it[0].toInt()]
        }
        symbolAtCoords.forEach{
            writer.writeOne(listOf(it))
        }
    }
}

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
}
data class Map(
    val rows: List<String>
) {
    val sizeX = rows[0].length
    val sizeY = rows.size
    fun getSymbol(coord: Coord): Char {
        return rows[coord.y][coord.x]
    }
    fun getAllWithSymbol(symbol: Char): Set<Coord> {
        val mutableLandCoords = mutableListOf<Coord>()

        // all coords iterate
        (0..<this.sizeX).forEach { x ->
            (0..<this.sizeY).forEach { y ->
                val coord = Coord(x, y)
                val isSymbol = this.getSymbol(coord) == symbol
                if (isSymbol) mutableLandCoords.add(coord)

            }
        }
        return mutableLandCoords.toSet()
    }

    fun getAllWater(): Set<Coord> {
        return getAllWithSymbol('W')
    }

}

fun main2(args: Array<String>) {
    DirectoryFilesRunner("C:\\Users\\Lucky13\\IdeaProjects\\catcoder-base\\src\\main\\resources\\level2").forEach { reader, writer ->
        val amountOfRows = reader.readOne()[0].toInt()
        val map = (0..<amountOfRows).map {
            reader.readOne()[0]
        }.let { Map(it) }

        val amountCoordinates = reader.readOne()[0].toInt()

        val coordinatePairs = (0..<amountCoordinates).map {
            reader.readOne().map {
                val parts = it.split(",")
                Coord(parts[0].toInt(), parts[1].toInt())
            }.toList()
        }



        val mutableLandCoords = mutableListOf<Coord>()

        // all coords iterate
        (0..<map.sizeX).forEach { x ->
            (0..<map.sizeY).forEach { y ->
                val coord = Coord(x, y)
                val symbol = map.getSymbol(coord)
                val isLand = symbol == 'L'
                if (isLand) mutableLandCoords.add(coord)

            }
        }
        val landCoords = mutableLandCoords.toList()

        val landCoordsHeap = landCoords.toMutableSet()

        val islands: MutableList<Set<Coord>> = mutableListOf()
        while(landCoordsHeap.size > 0) {
            val currentCoord = landCoordsHeap.first()
            val connectedCoords = currentCoord.getConnectedCoords().filter { landCoords.contains(it) }

            // check if connectedCoords are part of an island
            val connectedIslands = connectedCoords.mapNotNull { connectedCoord ->
                islands.filter { it.contains(connectedCoord) }.firstOrNull()
            }.toSet()

            // check if they are land
            val newIsland = mutableSetOf<Coord>()
            connectedIslands.forEach { newIsland.addAll(it) }
            newIsland.add(currentCoord)
            newIsland.addAll(connectedCoords)
            islands.removeAll(connectedIslands)
            islands.add(newIsland)

            landCoordsHeap.remove(currentCoord)
            //connectedCoords.forEach { landCoordsHeap.remove(it) }

        }

        val coordsCountDebug = islands.flatMap { it -> it }.groupBy { it }.mapValues { it.value.count() }.entries.sortedByDescending { it.value }


        val coordinateToIsland = islands.flatMap { island ->
            island.map { Pair(it, island) }
        }.toMap()

        coordinatePairs.forEach { coordinatePair ->
            val firstCoord = coordinatePair[0]
            val secondCoord = coordinatePair[1]

            val firstCoordIsland = coordinateToIsland[firstCoord]
            val secondCoordIsland = coordinateToIsland[secondCoord]
            val isSame = firstCoordIsland == secondCoordIsland

            writer.writeOne(
                if (isSame) "SAME" else "DIFFERENT"
            )
        }
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

fun main3(args: Array<String>) {
    DirectoryFilesRunner("C:\\Users\\Lucky13\\IdeaProjects\\catcoder-base\\src\\main\\resources\\level3").forEach { reader, writer ->
        val amountOfRows = reader.readOne()[0].toInt()
        val map = (0..<amountOfRows).map {
            reader.readOne()[0]
        }.let { Map(it) }

        val amountPaths = reader.readOne()[0].toInt()

        val paths = (0..<amountPaths).map {
            reader.readOne().map {
                val parts = it.split(",")
                Coord(parts[0].toInt(), parts[1].toInt())
            }.toList().let { Path(it) }
        }

        paths.forEach {
            if (it.isValid()) {
                writer.writeOne("VALID")
            } else {
                writer.writeOne("INVALID")
            }
        }
    }
}

fun main4(args: Array<String>) {
    DirectoryFilesRunner("C:\\Users\\Lucky13\\IdeaProjects\\catcoder-base\\src\\main\\resources\\level4").forEach { reader, writer ->
        val amountOfRows = reader.readOne()[0].toInt()
        val map = (0..<amountOfRows).map {
            reader.readOne()[0]
        }.let { Map(it) }

        val amountCoordinatePairs = reader.readOne()[0].toInt()

        val coordinatePairs = (0..<amountCoordinatePairs).map {
            reader.readOne().map {
                val parts = it.split(",")
                Coord(parts[0].toInt(), parts[1].toInt())
            }.toList()
        }
        val allWaterPoints = map.getAllWater()

        val maxLengthOfPath = amountOfRows * 2



        coordinatePairs.forEach { coordinatePair ->
            val start = coordinatePair[0]
            val destination = coordinatePair[1]

            val possibleSolutions = mutableListOf<Path>()
            val coordLengthCache = mutableMapOf<Coord, Int>()

            fun recursiveWhatever(pathUntilNow: Path) {
                if (possibleSolutions.isNotEmpty()) return;
                // is termination
                val last = pathUntilNow.coords.last()
                if ( last == destination) {
                    possibleSolutions.add(pathUntilNow)
                    return
                }

                if (pathUntilNow.size >= maxLengthOfPath) {
                    println("Path too long")
                    return
                }


                val neighbors = last.getConnectedCoordsWithDiagonal()
                    .filter { allWaterPoints.contains(it) }
                    .filter { !pathUntilNow.coords.contains(it) } // check if visited already

                val allPossibleNewPaths = neighbors.map { pathUntilNow.add(it) }
                    .filter { coordLengthCache[it.coords.last()] ?: Int.MAX_VALUE > it.coords.size }
                    .filter { it.isValid() }
                    .sortedBy {
                    val lastPoint = it.coords.last()
                    (lastPoint.x - destination.x).toDouble().pow(2) + (lastPoint.y - destination.y).toDouble().pow(2)
                }
                if (allPossibleNewPaths.isEmpty()) println("Deadend")

                allPossibleNewPaths.forEach { possibleNewPath ->
                    coordLengthCache[possibleNewPath.coords.last()] = possibleNewPath.size
                    recursiveWhatever(possibleNewPath)
                }
            }
            recursiveWhatever(Path(listOf(start)))
            writer.writeOne(possibleSolutions[0].coords.map { "${it.x},${it.y}" })
            // get all possible points to go to from start
            // must be water
            // must be on the map
        }
    }
}

fun main(args: Array<String>) {
    DirectoryFilesRunner("C:\\Users\\Lucky13\\IdeaProjects\\catcoder-base\\src\\main\\resources\\level1").forEach { reader, writer ->
        val amountOfRows = reader.readOne()[0].toInt()
        val map = (0..<amountOfRows).map {
            reader.readOne()[0]
        }.let { Map(it) }

        val amountCoordinates = reader.readOne()[0].toInt()

        val coordinates = (0..<amountCoordinates).map {
            reader.readOne().first().let {
                val parts = it.split(",")
                Coord(parts[0].toInt(), parts[1].toInt())
            }
        }



        val mutableLandCoords = mutableListOf<Coord>()

        // all coords iterate
        (0..<map.sizeX).forEach { x ->
            (0..<map.sizeY).forEach { y ->
                val coord = Coord(x, y)
                val symbol = map.getSymbol(coord)
                val isLand = symbol == 'L'
                if (isLand) mutableLandCoords.add(coord)

            }
        }
        val landCoords = mutableLandCoords.toList()

        val landCoordsHeap = landCoords.toMutableSet()

        // detect islands
        val islands: MutableList<Set<Coord>> = mutableListOf()
        while(landCoordsHeap.size > 0) {
            val currentCoord = landCoordsHeap.first()
            val connectedCoords = currentCoord.getConnectedCoords().filter { landCoords.contains(it) }

            // check if connectedCoords are part of an island
            val connectedIslands = connectedCoords.mapNotNull { connectedCoord ->
                islands.filter { it.contains(connectedCoord) }.firstOrNull()
            }.toSet()

            // check if they are land
            val newIsland = mutableSetOf<Coord>()
            connectedIslands.forEach { newIsland.addAll(it) }
            newIsland.add(currentCoord)
            newIsland.addAll(connectedCoords)
            islands.removeAll(connectedIslands)
            islands.add(newIsland)

            landCoordsHeap.remove(currentCoord)
            //connectedCoords.forEach { landCoordsHeap.remove(it) }

        }


        val coordinateToIsland = islands.flatMap { island ->
            island.map { Pair(it, island) }
        }.toMap()

        val allWater = map.getAllWater()

        coordinates.forEach {
            val island = coordinateToIsland[it]!!
            val allWaterPointsNextToIsland = island.flatMap { coord ->
                coord.getConnectedCoords().filter { allWater.contains(it) }
            }.toSet()
            val startPoint = allWaterPointsNextToIsland.first()

            val destination =
                startPoint.getConnectedCoordsWithDiagonal().filter { allWaterPointsNextToIsland.contains(it) }.first()

            // recursive shit
            val possibleSolutions = mutableListOf<Path>()

            fun recursiveWhatever(pathUntilNow: Path) {
                if (possibleSolutions.isNotEmpty()) return;
                // is termination
                val last = pathUntilNow.coords.last()
                if ( last == destination && pathUntilNow.size > 2 && pathUntilNow.coords.toSet() ==  allWaterPointsNextToIsland) { // todo: need to check if island is in
                    possibleSolutions.add(pathUntilNow)
                    return
                }


                val neighbors = last.getConnectedCoordsWithDiagonal()
                    .filter { allWaterPointsNextToIsland.contains(it) }
                    .filter { !pathUntilNow.coords.contains(it) } // check if visited already

                val allPossibleNewPaths = neighbors.map { pathUntilNow.add(it) }
                if (allPossibleNewPaths.isEmpty()) println("Deadend")

                allPossibleNewPaths.forEach { possibleNewPath ->
                    recursiveWhatever(possibleNewPath)
                }
            }
            recursiveWhatever(listOf(startPoint).let { Path(it) })
            possibleSolutions.first().let {
                writer.writeOne(it.coords.map { "${it.x},${it.y}" })
            }
            // rescuriv shit
        }
    }
}