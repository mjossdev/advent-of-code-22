fun main() {
    data class Cube(val x: Int, val y: Int, val z: Int) {
        override fun toString(): String = "$x,$y,$z"
    }

    fun Cube.adjacents() = listOf(
        copy(x = x + 1),
        copy(x = x - 1),
        copy(y = y + 1),
        copy(y = y - 1),
        copy(z = z + 1),
        copy(z = z - 1)
    )

    fun readCubes(input: List<String>): List<Cube> =
        input.map { line -> line.split(',').map { it.toInt() }.let { (x, y, z) -> Cube(x, y, z) } }.distinct()

    fun getGrid(cubes: Iterable<Cube>): Array<Array<BooleanArray>> {
        val width = cubes.maxOf { it.x } + 1
        val height = cubes.maxOf { it.y } + 1
        val depth = cubes.maxOf { it.z } + 1
        val grid = Array(width) { Array(height) { BooleanArray(depth) } }
        for ((x, y, z) in cubes) {
            grid[x][y][z] = true
        }
        return grid
    }

    fun part1(input: List<String>): Int {
        val cubes = readCubes(input)
        val grid = getGrid(cubes)
        val width = grid.size
        val height = grid[0].size
        val depth = grid[0][0].size
        return cubes.sumOf { (x, y, z) ->
            var sides = 6
            if (x > 0 && grid[x - 1][y][z]) --sides
            if (x < width - 1 && grid[x + 1][y][z]) --sides
            if (y > 0 && grid[x][y - 1][z]) --sides
            if (y < height - 1 && grid[x][y + 1][z]) --sides
            if (z > 0 && grid[x][y][z - 1]) --sides
            if (z < depth - 1 && grid[x][y][z + 1]) --sides
            sides
        }
    }

    fun part2(input: List<String>): Int {
        val cubes = readCubes(input)
        val grid = getGrid(cubes)
        val width = grid.size
        val height = grid[0].size
        val depth = grid[0][0].size

        fun Cube.isAir() = !(grid.elementAtOrNull(x)?.elementAtOrNull(y)?.elementAtOrNull(z) ?: false)

        fun countReachableSides(cube: Cube): Int = cube.adjacents().filter { it.isAir() }.count { start ->
            val queue = ArrayDeque<Cube>()
            val explored = mutableSetOf(start)
            queue.addLast(start)
            while (queue.isNotEmpty()) {
                val next = queue.removeFirst()
                if (next.run { x == 0 || x == width - 1 || y == 0 || y == height - 1 || z == 0 || z == depth - 1 }) {
                    return@count true
                }
                next.adjacents().filter { it.isAir() && it !in explored }.forEach {
                    explored.add(it)
                    queue.addLast(it)
                }
            }
            false
        }
        return cubes.sumOf { countReachableSides(it) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day18_test")
    check(part1(testInput) == 64)
    check(part2(testInput) == 58)

    val input = readInput("Day18")
    println(part1(input))
    println(part2(input))
}
