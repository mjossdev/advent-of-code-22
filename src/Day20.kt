fun main() {
    // used to allow finding the right instance of a number with instanceOf
    class IdentityLong(val value: Long) {
        override fun toString(): String = value.toString()
    }

    fun decrypt(numbers: List<IdentityLong>, times: Int): Long {
        val workingNumbers = numbers.toMutableList()

        repeat(times) {
            for (n in numbers) {
                val index = workingNumbers.indexOf(n)
                var newIndex = ((index + n.value) % workingNumbers.lastIndex).toInt()
                if (newIndex <= 0) {
                    newIndex += workingNumbers.lastIndex
                }

                if (index == newIndex) continue

                workingNumbers.removeAt(index)
                workingNumbers.add(newIndex, n)
            }
        }
        val indexOf0 = workingNumbers.indexOfFirst { it.value == 0L}
        return listOf(1000, 2000, 3000).sumOf {
            workingNumbers[(indexOf0 + it) % numbers.size].value
        }
    }

    fun part2(input: List<String>): Long = decrypt(input.map { IdentityLong(it.toLong() * 811589153) }, 10)

    fun part1(input: List<String>): Long = decrypt(input.map { IdentityLong(it.toLong()) }, 1)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day20_test")
    check(part1(testInput) == 3L)
    check(part2(testInput) == 1623178306L)

    val input = readInput("Day20")
    println(part1(input))
    println(part2(input))
}
