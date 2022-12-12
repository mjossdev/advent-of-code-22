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

private class AdabtablePriorityQueue<K : Comparable<K>, V> {
    private val heap = mutableListOf<EntryImpl>()
    val size get() = heap.size

    fun insert(key: K, value: V): Entry<K, V> {
        val entry = EntryImpl(key, value, size + 1)
        heap.add(entry)
        upHeap(size)
        return entry
    }

    private fun index(virtualIndex: Int) = virtualIndex - 1

    private tailrec fun upHeap(i: Int) {
        if (i == 1) return
        val entry = heap[index(i)]
        val parent = heap[index(i / 2)]
        if (entry.key < parent.key) {
            swap(i, i / 2)
            upHeap(i / 2)
        }
    }

    private fun swap(i: Int, j: Int) {
        val temp = heap[index(i)]
        heap[index(i)] = heap[index(j)]
        heap[index(j)] = temp
        heap[index(i)].index = i
        heap[index(j)].index = j
    }

    fun replaceKey(entry: Entry<K, V>, newKey: K): K {
        if (entry !is EntryImpl) {
            throw IllegalArgumentException("Entry does not belong to this queue")
        }
        val oldKey = entry.key
        entry.key = newKey
        val cmp = newKey compareTo oldKey
        if (cmp < 0) {
            upHeap(entry.index)
        } else if (cmp > 0) {
            downHeap(entry.index)
        }
        return oldKey
    }

    fun removeMin(): Entry<K, V> = remove(heap[index(1)])

    private tailrec fun downHeap(i: Int) {
        if (i * 2 > size) {
            return
        }
        val entry = heap[index(i)]
        val leftChild = heap[index(i * 2)]
        val rightChild = heap.getOrNull(index(i * 2 + 1))
        val smallerChild = if (rightChild == null || leftChild.key <= rightChild.key) {
            leftChild
        } else {
            rightChild
        }
        if (entry.key >= smallerChild.key) {
            swap(i, smallerChild.index)
            downHeap(entry.index)
        }
    }

    fun remove(entry: Entry<K, V>): Entry<K, V> {
        if (entry !is EntryImpl) {
            throw IllegalArgumentException("Entry does not belong to this queue")
        }
        val i = entry.index
        swap(i, size)
        heap.removeAt(index(size))
        if (isEmpty()) {
            return entry
        }
        val replacement = heap[index(i)]
        if (replacement.key < entry.key) {
            upHeap(i)
        } else {
            downHeap(i)
        }
        return entry
    }

    fun isEmpty() = size == 0

    interface Entry<K, V> {
        val key: K
        val value: V

        operator fun component1() = key
        operator fun component2() = value
    }

    private inner class EntryImpl(override var key: K, override var value: V, var index: Int) : Entry<K, V>
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

        val priorityQueue = AdabtablePriorityQueue<Int, Coordinate>()
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
                    val entry = locators[it.row][it.col]
                    priorityQueue.replaceKey(entry, distance + 1)
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
