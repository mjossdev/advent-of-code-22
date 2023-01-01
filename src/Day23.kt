private enum class MoveDirection {
    NORTH, SOUTH, WEST, EAST;
}

fun main() {
    data class Point(val row: Int, val col: Int)

    fun Point.next(direction: MoveDirection) = when (direction) {
        MoveDirection.NORTH -> copy(row = row - 1)
        MoveDirection.SOUTH -> copy(row = row + 1)
        MoveDirection.WEST -> copy(col = col - 1)
        MoveDirection.EAST -> copy(col = col + 1)
    }

    data class Move(val from: Point, val to: Point)

    fun readElfPositions(input: List<String>) = buildSet {
        input.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, cell ->
                if (cell == '#') add(Point(rowIndex, colIndex))
            }
        }
    }

    fun getProposedMoves(elfPositions: Set<Point>, directions: Iterable<MoveDirection>): List<Move> =
        elfPositions.map { position ->
            val stays = position.let {
                val north = position.next(MoveDirection.NORTH)
                val northWest = north.next(MoveDirection.WEST)
                val northEast = north.next(MoveDirection.EAST)
                val south = position.next(MoveDirection.SOUTH)
                val southWest = south.next(MoveDirection.WEST)
                val southEast = south.next(MoveDirection.EAST)
                val west = position.next(MoveDirection.WEST)
                val east = position.next(MoveDirection.EAST)
                arrayOf(
                    north,
                    northWest,
                    northEast,
                    south,
                    southWest,
                    southEast,
                    west,
                    east
                ).none { it in elfPositions }
            }
            if (stays) return@map Move(position, position)

            val direction = directions.firstOrNull {
                val newPosition = position.next(it)
                when (it) {
                    MoveDirection.NORTH, MoveDirection.SOUTH -> arrayOf(
                        newPosition,
                        newPosition.next(MoveDirection.WEST),
                        newPosition.next(MoveDirection.EAST)
                    ).none { p -> p in elfPositions }

                    MoveDirection.WEST, MoveDirection.EAST -> arrayOf(
                        newPosition,
                        newPosition.next(MoveDirection.NORTH),
                        newPosition.next(MoveDirection.SOUTH)
                    ).none { p -> p in elfPositions }
                }
            }
            if (direction == null) Move(position, position) else Move(position, position.next(direction))
        }


    fun part1(input: List<String>): Int {
        var elfPositions = readElfPositions(input)
        val directions = ArrayDeque(MoveDirection.values().toList())

        repeat(10) {
            elfPositions = getProposedMoves(elfPositions, directions).groupBy { it.to }.asSequence()
                .flatMap { (destination, moves) ->
                    if (moves.size == 1) listOf(destination) else moves.map { it.from }
                }.toSet()
            directions.addLast(directions.removeFirst())
        }
        return elfPositions.let {
            (it.maxOf { p -> p.row } - it.minOf { p -> p.row } + 1) * (it.maxOf { p -> p.col } - it.minOf { p -> p.col } + 1) - it.size
        }
    }

    fun part2(input: List<String>): Int {
        var elfPositions = readElfPositions(input)
        val directions = ArrayDeque(MoveDirection.values().toList())
        var round = 0
        while (true) {
            ++round
            val moves = getProposedMoves(elfPositions, directions)
            if (moves.all { (from, to) -> from == to }) return round
            elfPositions = moves.groupBy { it.to }.asSequence()
                .flatMap { (destination, moves) ->
                    if (moves.size == 1) listOf(destination) else moves.map { it.from }
                }.toSet()
            directions.addLast(directions.removeFirst())
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day23_test")
    check(part1(testInput) == 110)
    check(part2(testInput) == 20)

    val input = readInput("Day23")
    println(part1(input))
    println(part2(input))
}
