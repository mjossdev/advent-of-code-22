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

    fun part1(input: List<String>): Int {
        val commands = readCommands(input.last { it.isNotBlank() })
        val grid = readGrid(input.takeWhile { it.isNotBlank() })

        fun tileAt(point: Point) = grid.getOrNull(point.row)?.getOrNull(point.col) ?: Tile.VOID
        fun Point.next(facing: Facing): Point {
            val next = when (facing) {
                Facing.RIGHT -> copy(col = col + 1)
                Facing.DOWN -> copy(row = row + 1)
                Facing.LEFT -> copy(col = col - 1)
                Facing.UP -> copy(row = row - 1)
            }
            return when (tileAt(next)) {
                Tile.OPEN, Tile.WALL -> next
                Tile.VOID -> when (facing) {
                    Facing.RIGHT -> copy(col = grid[row].indexOfFirst { it != Tile.VOID })
                    Facing.DOWN -> copy(row = grid.indexOfFirst { it.getOrElse(col) { Tile.VOID } != Tile.VOID })
                    Facing.LEFT -> copy(col = grid[row].indexOfLast { it != Tile.VOID })
                    Facing.UP -> copy(row = grid.indexOfLast { it.getOrElse(col) { Tile.VOID } != Tile.VOID })
                }
            }
        }

        var currentPosition = Point(0, grid[0].indexOf(Tile.OPEN))
        var currentFacing = Facing.RIGHT
        for (command in commands) {
            when (command) {
                Command.TurnLeft -> currentFacing = currentFacing.turnLeft()
                Command.TurnRight -> currentFacing = currentFacing.turnRight()
                is Command.Move -> {
                    for (x in 0 until command.distance) {
                        val next = currentPosition.next(currentFacing)
                        when (tileAt(next)) {
                            Tile.OPEN -> currentPosition = next
                            Tile.WALL -> break
                            Tile.VOID -> error("WTF")
                        }
                    }
                }
            }
        }
        val (row, col) = currentPosition
        return 1000 * (row + 1) + 4 * (col + 1) + currentFacing.ordinal
    }

//    fun part2(input: List<String>): Int

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day22_test")
    check(part1(testInput) == 6032)
//    check(part2(testInput) == 5031)

    val input = readInput("Day22")
    println(part1(input))
//    println(part2(input))
}
