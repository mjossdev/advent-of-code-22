fun main() {
    fun String.findMarker(markerLength: Int): Int {
        for (i in markerLength..length) {
            if (substring(i - markerLength, i).toSet().size == markerLength) {
                return i
            }
        }
        throw IllegalArgumentException()
    }

    fun part1(input: String): Int = input.findMarker(4)

    fun part2(input: String): Int = input.findMarker(14)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test").single()
    check(part1(testInput) == 7)
    check(part2(testInput) == 19)

    val input = readInput("Day06").single()
    println(part1(input))
    println(part2(input))
}
