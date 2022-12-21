import kotlin.math.abs
import kotlin.math.sign

fun main() {
    // used to allow finding the right instance of a number with instanceOf
    class IdentityInt(val value: Int) {
        override fun toString(): String = value.toString()
    }

    /*
    1, 2, 4

    2, 1, 4

    1, 2, 4
    1, 4, 2

    1, 2, 4
    4, 2, 1
    2, 4, 1
    2, 1, 4

     */

    fun part1(input: List<String>): Int {
        val numbers = input.map { IdentityInt(it.toInt()) }
        val workingNumbers = numbers.toMutableList()
        for (n in numbers) {
            val step = n.value.sign
            var index = workingNumbers.indexOf(n)

            repeat(abs(n.value % numbers.lastIndex)) {
                var swapIndex = index + step
                if (swapIndex == -1) {
                    swapIndex = workingNumbers.lastIndex
                }
                if (swapIndex == workingNumbers.size) {
                    swapIndex = 0
                }
                workingNumbers[index] = workingNumbers[swapIndex]
                workingNumbers[swapIndex] = n
                index = swapIndex
            }
        }
        println(workingNumbers)
        val indexOf0 = workingNumbers.indexOfFirst { it.value == 0 }
        // -132
        // -780
        // 7552
        return listOf(1000, 2000, 3000).sumOf {
            println(workingNumbers[(indexOf0 + it) % numbers.size])
            workingNumbers[(indexOf0 + it) % numbers.size].value
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day20_test")
    check(part1(testInput) == 3)
    part1(listOf("1", "2", "3"))
//    check(part2(testInput) == 58)

    val input = readInput("Day20")
    println(part1(input))
//    println(part2(input))
}
