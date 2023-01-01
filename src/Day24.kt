fun main() {
    fun Char.toDirection() = when (this) {
        '^' -> Direction.UP
        'v' -> Direction.DOWN
        '<' -> Direction.LEFT
        '>' -> Direction.RIGHT
        else -> error("$this is not a direction")
    }

    data class Point(val row: Int, val col: Int)

    fun Point.next(direction: Direction) = when (direction) {
        Direction.UP -> copy(row = row - 1)
        Direction.DOWN -> copy(row = row + 1)
        Direction.LEFT -> copy(col = col - 1)
        Direction.RIGHT -> copy(col = col + 1)
    }

    data class Blizzard(val position: Point, val direction: Direction)
    data class Valley(val start: Point, val goal: Point, val walls: Set<Point>, val blizzards: Set<Blizzard>)

    fun readValley(input: List<String>): Valley {
        var start: Point? = null
        var goal: Point? = null
        val walls = mutableSetOf<Point>()
        val blizzards = mutableSetOf<Blizzard>()
        input.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, cell ->
                val position = Point(rowIndex, colIndex)
                when (cell) {
                    '#' -> walls.add(position)
                    '.' -> {
                        when (rowIndex) {
                            0 -> start = position
                            input.lastIndex -> goal = position
                        }
                    }

                    else -> blizzards.add(Blizzard(position, cell.toDirection()))
                }
            }
        }
        return Valley(start!!, goal!!, walls, blizzards)
    }

    fun calculateMinutes(valley: Valley, goals: List<Point>): Int {
        val walls = valley.walls
        val rightEdge = walls.maxOf { it.col }
        val bottomEdge = walls.maxOf { it.row }
        fun Point.isInBounds() = row in 0..bottomEdge && col in 0..rightEdge

        val directions = Direction.values().toList()

        var possiblePositions = setOf(valley.start)
        var blizzards = valley.blizzards.groupBy { it.position }

        var minutes = 0

        for (goal in goals) {
            while (possiblePositions.none { it == goal }) {
                blizzards = blizzards.values.asSequence().flatMap {
                    it.map { blizzard ->
                        val (position, direction) = blizzard
                        val nextPosition = position.next(direction)
                        blizzard.copy(
                            position = if (nextPosition in walls) {
                                when (direction) {
                                    Direction.UP -> position.copy(row = bottomEdge - 1)
                                    Direction.DOWN -> position.copy(row = 1)
                                    Direction.LEFT -> position.copy(col = rightEdge - 1)
                                    Direction.RIGHT -> position.copy(col = 1)
                                }
                            } else {
                                nextPosition
                            }
                        )
                    }
                }.groupBy { it.position }
                possiblePositions = possiblePositions.asSequence().flatMap { p ->
                    sequence {
                        directions.forEach {
                            val nextPosition = p.next(it)
                            if (nextPosition.isInBounds() && nextPosition !in walls && nextPosition !in blizzards) {
                                yield(nextPosition)
                            }
                        }
                        if (p !in blizzards) {
                            yield(p)
                        }
                    }
                }.toSet()
                ++minutes
            }
            possiblePositions = setOf(goal)
        }
        return minutes
    }


    fun part1(input: List<String>): Int {
        val valley = readValley(input)
        return calculateMinutes(valley, listOf(valley.goal))
    }
    fun part2(input: List<String>): Int {
        val valley = readValley(input)
        return calculateMinutes(valley, listOf(valley.goal, valley.start, valley.goal))
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day24_test")
    check(part1(testInput) == 18)
    check(part2(testInput) == 54)

    val input = readInput("Day24")
    println(part1(input))
    println(part2(input))
}
