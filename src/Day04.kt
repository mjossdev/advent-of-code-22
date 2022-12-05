fun main() {
    fun readAssignmentPairs(input: List<String>): List<Pair<IntRange, IntRange>> = input.map { line ->
        line.split(',').map { range ->
            val (from, to) = range.split('-').map { it.toInt() }
            from..to
        }.toPair()
    }

    fun IntRange.isFullyContainedWithin(other: IntRange) = first >= other.first && last <= other.last
    fun IntRange.hasOverlap(other: IntRange) = first in other || last in other || other.first in this || other.last in this

    fun part1(input: List<String>): Int = readAssignmentPairs(input).count { (fst, snd) -> fst.isFullyContainedWithin(snd) || snd.isFullyContainedWithin(fst) }

    fun part2(input: List<String>): Int = readAssignmentPairs(input).count { (fst, snd) -> fst.hasOverlap(snd) }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 2)
    check(part2(testInput) == 4)

    val input = readInput("Day04")
    println(part1(input))
    println(part2(input))
}
