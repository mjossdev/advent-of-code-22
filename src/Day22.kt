private enum class Tile {
    VOID, WALL, OPEN;
}

private sealed interface Command {
    object TurnLeft : Command {
        override fun toString(): String = "L"
    }

    object TurnRight : Command {
        override fun toString(): String = "R"
    }

    data class Move(val distance: Int) : Command {
        override fun toString(): String = distance.toString()
    }
}

private enum class Facing {
    RIGHT, DOWN, LEFT, UP;

    fun turnRight() = when (this) {
        RIGHT -> DOWN
        DOWN -> LEFT
        LEFT -> UP
        UP -> RIGHT
    }

    fun turnLeft() = when (this) {
        RIGHT -> UP
        DOWN -> RIGHT
        LEFT -> DOWN
        UP -> LEFT
    }
}

fun main() {
    data class Point(val row: Int, val col: Int)
    fun Point.next(facing: Facing) = when (facing) {
        Facing.RIGHT -> copy(col = col + 1)
        Facing.DOWN -> copy(row = row + 1)
        Facing.LEFT -> copy(col = col - 1)
        Facing.UP -> copy(row = row - 1)
    }
    data class State(val position: Point, val facing: Facing)
    fun Array<Array<Tile>>.tileAt(point: Point): Tile = getOrNull(point.row)?.getOrNull(point.col) ?: Tile.VOID

    fun readCommands(path: String): List<Command> = buildList {
        var currentNumber = 0
        for (c in path) {
            if (c.isDigit()) {
                currentNumber = currentNumber * 10 + c.digitToInt()
            } else {
                if (currentNumber > 0) {
                    add(Command.Move(currentNumber))
                    currentNumber = 0
                }
                add(
                    when (c) {
                        'L' -> Command.TurnLeft
                        'R' -> Command.TurnRight
                        else -> error("Unrecognized character: $c")
                    }
                )
            }
        }
        if (currentNumber > 0) {
            add(Command.Move(currentNumber))
        }
    }

    fun readGrid(input: List<String>): Array<Array<Tile>> = Array(input.size) { row ->
        Array(input[row].length) { col ->
            when (input[row][col]) {
                '#' -> Tile.WALL
                '.' -> Tile.OPEN
                else -> Tile.VOID
            }
        }
    }

    fun findPassword(grid: Array<Array<Tile>>, commands: List<Command>, moveForward: (State) -> State): Int {
        var currentState = State(Point(0, grid[0].indexOf(Tile.OPEN)), Facing.RIGHT)
        for (command in commands) {
            when (command) {
                Command.TurnLeft -> currentState = currentState.let { it.copy(facing = it.facing.turnLeft()) }
                Command.TurnRight -> currentState = currentState.let { it.copy(facing = it.facing.turnRight()) }
                is Command.Move -> {
                    for (x in 0 until command.distance) {
                        val next = moveForward(currentState)
                        when (grid.tileAt(next.position)) {
                            Tile.OPEN -> currentState = next
                            Tile.WALL -> break
                            Tile.VOID -> error("out of bounds")
                        }
                    }
                }
            }
        }
        val (row, col) = currentState.position
        return 1000 * (row + 1) + 4 * (col + 1) + currentState.facing.ordinal
    }

    fun part1(input: List<String>): Int {
        val grid = readGrid(input.takeWhile { it.isNotBlank() })
        val commands = readCommands(input.last { it.isNotBlank() })

        fun State.next(): State {
            val nextPosition = position.next(facing)
            return copy(position = when (grid.tileAt(nextPosition)) {
                Tile.OPEN, Tile.WALL -> nextPosition
                Tile.VOID -> when (facing) {
                    Facing.RIGHT -> position.copy(col = grid[position.row].indexOfFirst { it != Tile.VOID })
                    Facing.DOWN -> position.copy(row = grid.indexOfFirst { it.getOrElse(position.col) { Tile.VOID } != Tile.VOID })
                    Facing.LEFT -> position.copy(col = grid[position.row].indexOfLast { it != Tile.VOID })
                    Facing.UP -> position.copy(row = grid.indexOfLast { it.getOrElse(position.col) { Tile.VOID } != Tile.VOID })
                }
            })
        }
        return findPassword(grid, commands) { it.next() }
    }



    fun part2(input: List<String>, warps: Map<State, State>): Int
    {
        val grid = readGrid(input.takeWhile { it.isNotBlank() })
        val commands = readCommands(input.last { it.isNotBlank() })
        return findPassword(grid, commands) {
            val nextPosition = it.position.next(it.facing)
            val nextState = it.copy(position = nextPosition)
            when(grid.tileAt(nextPosition)) {
                Tile.WALL, Tile.OPEN -> nextState
                Tile.VOID -> warps.getValue(nextState)
            }
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day22_test")
    check(part1(testInput) == 6032)
    val testWarps = (0..3).asSequence().flatMap {
        sequenceOf(
            // top
            State(Point(-1, 8 + it), Facing.UP) to State(Point(4, 3 - it), Facing.DOWN),
            State(Point(it, 7), Facing.LEFT) to State(Point(4, 4 + it), Facing.DOWN),
            State(Point(it, 12), Facing.RIGHT) to State(Point(11 - it, 15), Facing.LEFT),
            // mid left
            State(Point(3, it), Facing.UP) to State(Point(0, 11 - it), Facing.DOWN),
            State(Point(4 + it, -1), Facing.LEFT) to State(Point(11, 15 - it), Facing.UP),
            State(Point(8, it), Facing.DOWN) to State(Point(11, 11 - it), Facing.UP),
            // mid mid
            State(Point(3, 4 + it), Facing.UP) to State(Point(it, 8), Facing.RIGHT),
            State(Point(8, 4 + it), Facing.DOWN) to State(Point(11 - it, 8), Facing.RIGHT),
            // mid right
            State(Point(4 + it, 12 ), Facing.RIGHT) to State(Point(8, 15 - it), Facing.DOWN),
            // bottom left
            State(Point(8 + it, 7), Facing.LEFT) to State(Point(7, 7 - it), Facing.UP),
            State(Point(12, 8 + it), Facing.DOWN) to State(Point(7, 3 - it), Facing.UP),
            // bottom right
            State(Point(7, 11 + it), Facing.UP) to State(Point(7 - it, 11), Facing.LEFT),
            State(Point(8 + it, 15), Facing.RIGHT) to State(Point(3 - it, 11), Facing.LEFT),
            State(Point(12, 11 + it), Facing.DOWN) to State(Point(7 - it, 0), Facing.RIGHT)
        )
    }.toMap()
    check(part2(testInput, testWarps) == 5031)

    val input = readInput("Day22")
    println(part1(input))
    val warps = (0..49).asSequence().flatMap {
        sequenceOf(
            // top left
            State(Point(-1, 50 + it), Facing.UP) to State(Point(150 + it, 0), Facing.RIGHT),
            State(Point(it, 49), Facing.LEFT) to State(Point(149 - it, 0), Facing.RIGHT),
            // top right
            State(Point(-1, 100 + it), Facing.UP) to State(Point(199, it), Facing.UP),
            State(Point(it, 150), Facing.RIGHT) to State(Point(149 - it, 99), Facing.LEFT),
            State(Point(50, 100 + it), Facing.DOWN) to State(Point(50 + it, 99), Facing.LEFT),
            // upper mid
            State(Point(50 + it, 49), Facing.LEFT) to State(Point(100, it), Facing.DOWN),
            State(Point(50 + it, 100), Facing.RIGHT) to State(Point(49, 100 + it), Facing.UP),
            // lower mid left
            State(Point(99, it), Facing.UP) to State(Point(50 + it, 50), Facing.RIGHT),
            State(Point(100 + it, -1), Facing.LEFT) to State(Point(49 - it, 50), Facing.RIGHT),
            // lower mid right
            State(Point(100 + it, 100), Facing.RIGHT) to State(Point(49 - it, 149), Facing.LEFT),
            State(Point(150, 50 + it), Facing.DOWN) to State(Point(150 + it, 49), Facing.LEFT),
            // bottom
            State(Point(150 + it, -1), Facing.LEFT) to State(Point(0, 50 + it), Facing.DOWN),
            State(Point(150 + it, 50), Facing.RIGHT) to State(Point(149, 50 + it), Facing.UP),
            State(Point(200, it), Facing.DOWN) to State(Point(0, 100 + it), Facing.DOWN)
        )
    }.toMap()
    println(part2(input, warps))
}
