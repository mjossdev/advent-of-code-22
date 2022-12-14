private enum class Unit {
    ROCK, SAND
}

fun main() {
    data class Coordinate(val x: Int, val y: Int) {
        override fun toString() = "$x,$y"
    }

    operator fun Coordinate.rangeTo(other: Coordinate) = when {
        this == other -> sequenceOf(this)
        x == other.x -> (if (y < other.y) y..other.y else y downTo other.y).asSequence().map { copy(y = it) }
        y == other.y -> (if (x < other.x) x..other.x else x downTo other.x).asSequence().map { copy(x = it) }
        else -> throw IllegalArgumentException()
    }

    fun String.toCoordinate() = split(',').let { (x, y) -> Coordinate(x.toInt(), y.toInt()) }

    fun readCoordinates(input: List<String>): Set<Coordinate> = input.flatMap { line ->
        line.split(" -> ")
            .zipWithNext()
            .flatMap { (s, e) -> s.toCoordinate()..e.toCoordinate() }
    }.toSet()

    fun part1(input: List<String>): Int {
        val cave = readCoordinates(input).associateWith { Unit.ROCK }.toMutableMap()
        val sandSpawn = Coordinate(500, 0)
        val lowestY = cave.keys.maxOf { it.y }
        var spawnedSand = 0
        while (true) {
            var current = sandSpawn
            while (current.y < lowestY) {
                var next = current.copy(y = current.y + 1)
                if (cave[next] != null) {
                    next = next.copy(x = current.x - 1)
                    if (cave[next] != null) {
                        next = next.copy(x = current.x + 1)
                        if (cave[next] != null) {
                            break
                        }
                    }
                }
                current = next
            }
            if (current.y >= lowestY) {
                return spawnedSand
            }
            cave[current] = Unit.SAND
            ++spawnedSand
        }
    }

    fun part2(input: List<String>): Int {
        val cave = readCoordinates(input).associateWith { Unit.ROCK }.toMutableMap()
        val lowestY = cave.keys.maxOf { it.y } + 2

        fun isBlocked(coordinate: Coordinate) = coordinate.y >= lowestY || cave[coordinate] != null

        val sandSpawn = Coordinate(500, 0)

        var spawnedSand = 0
        while (cave[sandSpawn] == null) {
            var current = sandSpawn
            while (current.y < lowestY) {
                var next = current.copy(y = current.y + 1)
                if (isBlocked(next)) {
                    next = next.copy(x = current.x - 1)
                    if (isBlocked(next)) {
                        next = next.copy(x = current.x + 1)
                        if (isBlocked(next)) {
                            break
                        }
                    }
                }
                current = next
            }
            cave[current] = Unit.SAND
            ++spawnedSand
        }
        return spawnedSand
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day14_test")
    check(part1(testInput) == 24)
    check(part2(testInput) == 93)

    val input = readInput("Day14")
    println(part1(input))
    println(part2(input))
}
