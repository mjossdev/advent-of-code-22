fun main() {
    fun readHeightMap(input: List<String>): Array<IntArray> =
        Array(input.size) { row -> IntArray(input[row].length) { input[row][it].digitToInt() } }

    fun Array<IntArray>.isVisible(row: Int, col: Int): Boolean {
        val height = this[row][col]
        return (0 until col).all { this[row][it] < height }
                || (col + 1..this[row].lastIndex).all { this[row][it] < height }
                || (0 until row).all { this[it][col] < height }
                || (row + 1..this.lastIndex).all { this[it][col] < height }
    }

    fun Array<IntArray>.scenicScore(row: Int, col: Int): Int {
        val height = this[row][col]
        val rowPredicate: (Int) -> Boolean = { this[it][col] >= height }
        val colPredicate: (Int) -> Boolean = { this[row][it] >= height }
        return (
            (row - 1 downTo 0).countUntil(rowPredicate)
            * (row + 1..lastIndex).countUntil(rowPredicate)
            * (col - 1 downTo 0).countUntil(colPredicate)
            * (col + 1..this[row].lastIndex).countUntil(colPredicate)
        )
    }

    fun part1(input: List<String>): Int {
        val heightMap = readHeightMap(input)
        return heightMap.indices.sumOf { row -> heightMap[row].indices.count { col -> heightMap.isVisible(row, col) } }
    }

    fun part2(input: List<String>): Int {
        val heightMap = readHeightMap(input)
        return heightMap.indices.maxOf { row ->
            heightMap[row].indices.maxOf { col ->
                heightMap.scenicScore(
                    row,
                    col
                )
            }
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 21)
    check(part2(testInput) == 8)

    val input = readInput("Day08")
    println(part1(input))
    println(part2(input))
}
