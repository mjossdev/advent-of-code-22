private enum class JetDirection(val delta: Int) {
    LEFT(-1), RIGHT(1)
}

private enum class RockShape {
    LINE, CROSS, J, I, SQUARE
}


fun main() {
    data class Point(val x: Int, val y: Int)

    fun Char.toDirection(): JetDirection = when (this) {
        '<' -> JetDirection.LEFT
        '>' -> JetDirection.RIGHT
        else -> error("$this is no direction")
    }

    fun createRock(shape: RockShape, leftX: Int, bottomY: Int): List<Point> = when (shape) {
        RockShape.LINE -> (0..3).map { Point(leftX + it, bottomY) }
        RockShape.CROSS -> (0..2).map { Point(leftX + it, bottomY + 1) } + listOf(Point(leftX + 1, bottomY), Point(leftX + 1, bottomY + 2))
        RockShape.J -> (0..2).map { Point(leftX + it, bottomY) } + (1..2).map { Point(leftX + 2, bottomY + it) }
        RockShape.I -> (0..3).map { Point(leftX, bottomY + it) }
        RockShape.SQUARE -> (0..1).flatMap { x -> (0..1).map { y -> Point(leftX + x, bottomY + y) } }
    }


    fun calculateHeight(numberOfRocks: Int, jets: List<JetDirection>): Int {
        val rockPoints = (0..6).map { Point(it, 0) }.toMutableSet()
        val shapeIterator = RockShape.values().toList().repeat().iterator()
        val jetIterator = jets.repeat().iterator()
        repeat (numberOfRocks) {
            var rock = createRock(shapeIterator.next(), 2,  (rockPoints.maxOfOrNull { it.y } ?: 0) + 4)
            while (true) {
                val direction = jetIterator.next()
                val movedByJet = rock.map { it.copy(x = it.x + direction.delta) }
                if (movedByJet.all { it.x in 0..6 && it !in rockPoints }) {
                    rock = movedByJet
                }
                val movedDown = rock.map { it.copy(y = it.y - 1) }
                if (movedDown.none { it in rockPoints }) {
                    rock = movedDown
                } else {
                    break
                }
            }
            rockPoints.addAll(rock)
        }
        return rockPoints.maxOf { it.y }
    }

    fun part1(input: List<String>): Int = calculateHeight(2022, input.single().map { it.toDirection() })

//    fun part2(input: List<String>): Int

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day17_test")
    check(part1(testInput) == 3068)
//    check(part2(testInput) == 1707)

    val input = readInput("Day17")
    println(part1(input))
//    println(part2(input))
}
