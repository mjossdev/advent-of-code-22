private enum class Operator(val apply: (Long, Long) -> Long) {
    PLUS(Long::plus), MINUS(Long::minus), TIMES(Long::times), DIV(Long::div)
}

private sealed interface MonkeyNumber {
    data class Constant(val value: Long) : MonkeyNumber

    data class Operation(val monkey1: String, val monkey2: String, val operator: Operator) : MonkeyNumber
}

fun main() {
    fun String.toOperator() = when (this) {
        "+" -> Operator.PLUS
        "-" -> Operator.MINUS
        "*" -> Operator.TIMES
        "/" -> Operator.DIV
        else -> error("$this is no operator")
    }

    fun readMonkeys(input: List<String>) = input.associate { line ->
        val (name, operation) = line.split(": ")
        name to if (operation.isNumber()) {
            MonkeyNumber.Constant(operation.toLong())
        } else {
            val (m1, op, m2) = operation.split(' ')
            MonkeyNumber.Operation(m1, m2, op.toOperator())
        }
    }

    fun part1(input: List<String>): Long {
        val monkeys = readMonkeys(input)

        fun evaluate(monkey: String): Long = monkeys.getValue(monkey).let {
            when (it) {
                is MonkeyNumber.Constant -> it.value
                is MonkeyNumber.Operation -> it.operator.apply(evaluate(it.monkey1), evaluate(it.monkey2))
            }
        }

        return evaluate("root")
    }

    fun part2(input: List<String>): Long {
        val monkeys = readMonkeys(input)
        fun hasHuman(monkey: String): Boolean = monkey == "humn" || monkeys.getValue(monkey).let {
            it is MonkeyNumber.Operation && (hasHuman(it.monkey1) || hasHuman(it.monkey2))
        }

        fun evaluate(monkey: String): Long = monkeys.getValue(monkey).let {
            when (it) {
                is MonkeyNumber.Constant -> it.value
                is MonkeyNumber.Operation -> it.operator.apply(evaluate(it.monkey1), evaluate(it.monkey2))
            }
        }

        fun solveFor(monkey: String, result: Long): Long {
            val operation = monkeys.getValue(monkey) as MonkeyNumber.Operation
            return if (hasHuman(operation.monkey1)) {
                val operand = evaluate(operation.monkey2)
                val subResult = when (operation.operator) {
                    Operator.PLUS -> result - operand
                    Operator.MINUS -> result + operand
                    Operator.TIMES -> result / operand
                    Operator.DIV -> result * operand
                }
                if (operation.monkey1 == "humn") {
                    subResult
                } else {
                    solveFor(operation.monkey1, subResult)
                }
            } else {
                val operand = evaluate(operation.monkey1)
                val subResult = when (operation.operator) {
                    Operator.PLUS -> result - operand
                    Operator.MINUS -> operand - result
                    Operator.TIMES -> result / operand
                    Operator.DIV -> operand / result
                }
                if (operation.monkey2 == "humn") {
                    subResult
                } else {
                    solveFor(operation.monkey2, subResult)
                }
            }
        }

        val rootOperation = monkeys.getValue("root") as MonkeyNumber.Operation
        return if (hasHuman(rootOperation.monkey1)) {
            val result = evaluate(rootOperation.monkey2)
            solveFor(rootOperation.monkey1, result)
        } else {
            val result = evaluate(rootOperation.monkey1)
            solveFor(rootOperation.monkey2, result)
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day21_test")
    check(part1(testInput) == 152L)
    check(part2(testInput) == 301L)

    val input = readInput("Day21")
    println(part1(input))
    println(part2(input))
}
