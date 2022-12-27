private enum class JetDirection(val delta: Int) {
    LEFT(-1), RIGHT(1)
}

private enum class RockShape {
    LINE, CROSS, J, I, SQUARE
}


fun main() {
    data class Point(val x: Long, val y: Long)

    fun Char.toDirection(): JetDirection = when (this) {
        '<' -> JetDirection.LEFT
        '>' -> JetDirection.RIGHT
        else -> error("$this is no direction")
    }

    fun createRock(shape: RockShape, leftX: Long, bottomY: Long): List<Point> = when (shape) {
        RockShape.LINE -> (0..3).map { Point(leftX + it, bottomY) }
        RockShape.CROSS -> (0..2).map { Point(leftX + it, bottomY + 1) } + listOf(
            Point(leftX + 1, bottomY),
            Point(leftX + 1, bottomY + 2)
        )

        RockShape.J -> (0..2).map { Point(leftX + it, bottomY) } + (1..2).map { Point(leftX + 2, bottomY + it) }
        RockShape.I -> (0..3).map { Point(leftX, bottomY + it) }
        RockShape.SQUARE -> (0..1).flatMap { x -> (0..1).map { y -> Point(leftX + x, bottomY + y) } }
    }

    fun calculateHeight(numberOfRocks: Long, jets: List<JetDirection>): Long {
        data class CacheValue(val height: Long, val droppedRocks: Long)
        data class CacheKey(val heightDifferences: List<Long>, val shapeIdx: Int, val jetIdx: Int)
        val cache = mutableMapOf<CacheKey, CacheValue>()

        var rockPoints = (0L..6L).map { Point(it, 0) }.toMutableSet()
        val shapes = RockShape.values().toList()
        var shapeIdx = 0
        var jetIdx = 0
        var rockIdx = 0L
        while (rockIdx < numberOfRocks) {
            val peaks = rockPoints.groupBy { it.x }.let { List(7) { i -> it.getValue(i.toLong()).maxOf { it.y } } }
            val highestPeak = peaks.max()
            val heightDifferences = peaks.map { it - highestPeak }
            val cacheKey = CacheKey(heightDifferences, shapeIdx, jetIdx)
            val cacheValue = cache[cacheKey]
            if (cacheValue != null) {
                val heightDiff = rockPoints.maxOf { it.y } - cacheValue.height
                val rockDiff = rockIdx - cacheValue.droppedRocks
                val remainingRocks = (numberOfRocks - rockIdx)
                val repetitions = remainingRocks / rockDiff
                rockPoints = rockPoints.asSequence().map { it.copy(y = it.y + heightDiff * repetitions) }.toMutableSet()
                rockIdx += repetitions * rockDiff
                if (rockIdx == numberOfRocks) break
            } else {
                cache[cacheKey] = CacheValue(rockPoints.maxOf { it.y }, rockIdx)
            }

            var rock = createRock(shapes[shapeIdx], 2, rockPoints.maxOf { it.y } + 4)
            while (true) {
                val direction = jets[jetIdx]
                jetIdx = (jetIdx + 1) % jets.size
                val movedByJet = rock.map { it.copy(x = it.x + direction.delta) }
                if (movedByJet.all { it.x in 0..6 && it !in rockPoints }) {
                    rock = movedByJet
                }
                val movedDown = rock.map { it.copy(y = it.y - 1) }
                if (movedDown.any { it in rockPoints }) {
                    break
                }
                rock = movedDown
            }
            rockPoints.addAll(rock)
            shapeIdx = (shapeIdx + 1) % shapes.size
            ++rockIdx
        }
        return rockPoints.maxOf { it.y }
    }

    fun part1(input: List<String>): Long = calculateHeight(2022, input.single().map { it.toDirection() })

    fun part2(input: List<String>): Long = calculateHeight(1000000000000, input.single().map { it.toDirection() })

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day17_test")
    check(part1(testInput) == 3068L)
    check(part2(testInput) == 1514285714288)

    val input = readInput("Day17")
    println(part1(input))
    println(part2(input))
}
