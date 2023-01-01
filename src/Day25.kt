import java.math.BigInteger

fun main() {
    fun readSnafuNumber(number: String): BigInteger {
        var factor = BigInteger.ONE
        var total = BigInteger.ZERO
        for (digit in number.reversed()) {
            val value = when (digit) {
                '=' -> -2
                '-' -> -1
                '0', '1', '2' -> digit.digitToInt()
                else -> error("$digit is not a SNAFU digit")
            }.toBigInteger()
            total += value * factor
            factor *= 5.toBigInteger()
        }
        return total
    }

    fun BigInteger.toSnafuNumber(): String {
        val digits = ArrayDeque<Char>()
        var remaining = this
        var power = BigInteger.ONE
        while (remaining != BigInteger.ZERO) {
            val nextPower = power * 5.toBigInteger()
            val current = remaining % nextPower
            val base5Digit = (current / power).intValueExact()
            if (base5Digit < 3) {
                digits.addFirst(base5Digit.digitToChar())
                remaining -= current
            } else {
                remaining += power
                when (base5Digit) {
                    3 -> {
                        remaining += power
                        digits.addFirst('=')
                    }

                    4 -> digits.addFirst('-')
                }
            }
            power = nextPower
        }
        val number = digits.joinToString("")
        check(readSnafuNumber(number) == this)
        return number
    }

    fun part1(input: List<String>): String = input.sumOf(::readSnafuNumber).toSnafuNumber()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day25_test")
    check(part1(testInput) == "2=-1=0")

    val input = readInput("Day25")
    println(part1(input))
}
