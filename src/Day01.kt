fun main() {
    fun readCalories(input: List<String>): List<Int> = buildList {
        var current = 0
        for (line in input) {
            if (line.isEmpty() && current > 0) {
                add(current)
                current = 0
            } else {
                current += line.toInt()
            }
        }
    }

    fun part1(input: List<String>): Int = readCalories(input).max()

    fun part2(input: List<String>): Int = readCalories(input).sortedDescending().take(3).sum()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 24000)
    check(part2(testInput) == 45000)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}
