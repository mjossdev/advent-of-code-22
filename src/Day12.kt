private sealed interface Point : Comparable<Point> {
    val elevation: Int

    override fun compareTo(other: Point): Int = elevation.compareTo(other.elevation)
}

private object StartPoint : Point {
    override val elevation: Int
        get() = 0

    override fun toString(): String = "S"
}

private object EndPoint : Point {
    override val elevation: Int
        get() = 25

    override fun toString(): String = "E"
}

private class NormalPoint(override val elevation: Int) : Point {
    init {
        check(elevation in 0..25)
    }

    override fun toString(): String = ('a' + elevation).toString()
}

fun main() {
    data class Coordinate(val row: Int, val col: Int)

    fun Coordinate.neighbors() =
        listOf(copy(row = row - 1), copy(row = row + 1), copy(col = col - 1), copy(col = col + 1))

    fun readHeightMap(input: List<String>) = Array(input.size) { row ->
        Array(input[row].length) { col ->
            input[row][col].let {
                when (it) {
                    'S' -> StartPoint
                    'E' -> EndPoint
                    else -> NormalPoint(it - 'a')
                }
            }
        }
    }

    fun Point.canReach(other: Point) = other.elevation <= elevation + 1

    fun findStartCoordinate(heightMap: Array<Array<Point>>): Coordinate {
        heightMap.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, point ->
                if (point == StartPoint) {
                    return Coordinate(rowIndex, colIndex)
                }
            }
        }
        error("no StartPoint")
    }

    fun findShortestPathToEnd(heightMap: Array<Array<Point>>, startCoordinate: Coordinate): Int? {
        fun Coordinate.isValid() = row in heightMap.indices && col in heightMap.first().indices

        val priorityQueue = AdaptablePriorityQueue<Int, Coordinate>()
        val distances = Array(heightMap.size) { row -> IntArray(heightMap[row].size) { Int.MAX_VALUE } }
        distances[startCoordinate.row][startCoordinate.col] = 0
        val locators = Array(heightMap.size) { row ->
            Array(heightMap[row].size) { col ->
                priorityQueue.insert(distances[row][col], Coordinate(row, col))
            }
        }
        while (!priorityQueue.isEmpty()) {
            val (distance, coordinate) = priorityQueue.removeMin()
            if (distance == Int.MAX_VALUE) return null
            if (heightMap[coordinate.row][coordinate.col] == EndPoint) {
                return distance
            }
            coordinate.neighbors()
                .filter { it.isValid() && heightMap[coordinate.row][coordinate.col].canReach(heightMap[it.row][it.col]) }
                .forEach {
                    if (distances[it.row][it.col] == Int.MAX_VALUE) {
                        val entry = locators[it.row][it.col]
                        priorityQueue.replaceKey(entry, distance + 1)
                        distances[it.row][it.col] = distance + 1
                    }
                }
        }
        error("EndPoint not found")
    }

    fun part1(input: List<String>): Int {
        val heightMap = readHeightMap(input)
        val startCoordinate = findStartCoordinate(heightMap)
        return findShortestPathToEnd(heightMap, startCoordinate)!!
    }

    fun part2(input: List<String>): Int {
        val heightMap = readHeightMap(input)
        val startPoints = buildList {
            heightMap.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { colIndex, point ->
                    if (point.elevation == 0) {
                        add(Coordinate(rowIndex, colIndex))
                    }
                }
            }
        }
        return startPoints.mapNotNull { findShortestPathToEnd(heightMap, it) }.min()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day12_test")
    check(part1(testInput) == 31)
    check(part2(testInput) == 29)

    val input = readInput("Day12")
    println(part1(input))
    println(part2(input))
}
