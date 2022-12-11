fun main() {
    class MultiModuloNumber private constructor(private val valueByModulus: Map<Int, Int>) {
        constructor(value: Int, moduluses: Iterable<Int>) : this(moduluses.associateWith { value % it })

        operator fun plus(x: Int) = MultiModuloNumber(valueByModulus.mapValues { (modulus, value) -> (value + x) % modulus })
        operator fun plus(x: MultiModuloNumber): MultiModuloNumber {
            check(valueByModulus.keys == x.valueByModulus.keys)
            return MultiModuloNumber(valueByModulus.mapValues { (modulus, value) -> (value + x.valueByModulus.getValue(modulus)) % modulus })
        }
        operator fun times(x: Int) = MultiModuloNumber(valueByModulus.mapValues { (modulus, value) -> (value * x) % modulus })
        operator fun times(x: MultiModuloNumber): MultiModuloNumber {
            check(valueByModulus.keys == x.valueByModulus.keys)
            return MultiModuloNumber(valueByModulus.mapValues { (modulus, value) -> (value * x.valueByModulus.getValue(modulus)) % modulus })
        }

        operator fun div(x: Int) = MultiModuloNumber(valueByModulus.mapValues { (modulus, value) -> (value / x) % modulus})

        operator fun rem(x: Int): Int = valueByModulus.getValue(x)

        fun withValue(x: Int) = MultiModuloNumber(valueByModulus.mapValues { (modulus) -> x % modulus })

        override fun toString(): String = valueByModulus.toString()
    }

    data class Throw(val monkeyIndex: Int, val itemWorryLevel: MultiModuloNumber)

    data class RawMonkey(val worryLevels: List<Int>, val inspectOperation: (MultiModuloNumber) -> MultiModuloNumber, val modulus: Int, val trueMonkey: Int, val falseMonkey: Int)

    class Monkey(
        initialWorryLevels: List<MultiModuloNumber>,
        private val inspectOperation: (MultiModuloNumber) -> MultiModuloNumber,
        private val modulus: Int,
        private val trueMonkey: Int,
        private val falseMonkey: Int
    ) {
        private val worryLevels = initialWorryLevels.toMutableList()
        var inspectedItems = 0L
            private set

        fun takeTurn(worryReducer: (MultiModuloNumber) -> MultiModuloNumber): List<Throw> {
            val result = worryLevels.map {
                val newLevel = worryReducer(inspectOperation(it))
                Throw(if (newLevel % modulus == 0) trueMonkey else falseMonkey, newLevel)
            }
            inspectedItems += worryLevels.size
            worryLevels.clear()
            return result
        }

        fun addItem(worryLevel: MultiModuloNumber) {
            worryLevels.add(worryLevel)
        }
    }

    class MonkeyGroup(monkeys: Iterable<RawMonkey>) {
        private val monkeys = monkeys.let {
            val moduluses = monkeys.map { it.modulus }
            monkeys.map { (levels, inspectOperation, modulus, trueMonkey, falseMonkey) ->
                val worryLevels = levels.map { MultiModuloNumber(it, moduluses) }
                Monkey(worryLevels, inspectOperation, modulus, trueMonkey, falseMonkey)
            }
        }

        val monkeyBusiness
            get() = monkeys.sortedByDescending { it.inspectedItems }
                .let { (fst, snd) -> fst.inspectedItems * snd.inspectedItems }

        fun runRound(worryReducer: (MultiModuloNumber) -> MultiModuloNumber) {
            for (monkey in monkeys) {
                val throws = monkey.takeTurn(worryReducer)
                throws.forEach { (index, worryLevel) -> monkeys[index].addItem(worryLevel) }
            }
        }
    }

    fun readOperation(operation: String): (MultiModuloNumber) -> MultiModuloNumber {
        val (operand1, operator, operand2) = operation.split(' ')
        val func: (MultiModuloNumber, MultiModuloNumber) -> MultiModuloNumber = when (operator) {
            "+" -> MultiModuloNumber::plus
            "*" -> MultiModuloNumber::times
            else -> error("$operator is no operator")
        }
        return {
            func(
                if (operand1 == "old") it else it.withValue(operand1.toInt()),
                if (operand2 == "old") it else it.withValue(operand2.toInt())
            )
        }
    }

    fun readMonkeys(input: List<String>): MonkeyGroup = input.chunked(7) { lines ->
        val startingItems = lines[1].split(": ").last().split(", ").map { it.toInt() }
        val operation = readOperation(lines[2].split(" = ").last())
        val modulus = lines[3].split(' ').last().toInt()
        val trueMonkey = lines[4].split(' ').last().toInt()
        val falseMonkey = lines[5].split(' ').last().toInt()
        RawMonkey(startingItems, operation, modulus, trueMonkey, falseMonkey)
    }.let { MonkeyGroup(it) }

    fun calculateMonkeyBusiness(
        monkeyGroup: MonkeyGroup,
        rounds: Int,
        worryReducer: (MultiModuloNumber) -> MultiModuloNumber = { it }
    ): Long {
        repeat(rounds) {
            monkeyGroup.runRound(worryReducer)
        }
        return monkeyGroup.monkeyBusiness
    }

    fun part1(input: List<String>): Long = calculateMonkeyBusiness(readMonkeys(input), 20) { it / 3}

    fun part2(input: List<String>): Long = calculateMonkeyBusiness(readMonkeys(input), 10000)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day11_test")
    check(part1(testInput) == 10605L)
    check(part2(testInput) == 2713310158)

    val input = readInput("Day11")
    println(part1(input))
    println(part2(input))
}
