private sealed interface Instruction {
    val cycles: Int
}

private class Addx(val value: Int) : Instruction {
    override val cycles
        get() = 2
}

private object Noop : Instruction {
    override val cycles: Int
        get() = 1
}

private const val ROW_WIDTH = 40

fun main() {
    class Cpu {
        private val valuesOfX = mutableListOf(1)
        val cycles
            get() = valuesOfX.size - 1

        fun getXDuring(cycle: Int): Int = valuesOfX[cycle - 1]

        fun execute(instruction: Instruction) {
            valuesOfX.apply {
                val x = last()
                repeat(instruction.cycles - 1) {
                    add(x)
                }
                add(when (instruction) {
                    Noop -> x
                    is Addx -> x + instruction.value
                })
            }
        }

        fun draw(): List<String> = valuesOfX.windowed(ROW_WIDTH, ROW_WIDTH).map {
            it.mapIndexed { cycle, spritePos ->
                if (cycle in spritePos - 1..spritePos + 1) '#' else '.'
            }.joinToString("")
        }
    }

    fun readInstructions(input: List<String>) = input.map { when (it) {
        "noop" -> Noop
        else -> it.split(' ').let { (inst, value) ->
            check(inst == "addx")
            Addx(value.toInt())
        }
    } }

    fun part1(input: List<String>): Int {
        val cpu = Cpu()
        readInstructions(input).forEach {
            cpu.execute(it)
        }
        return (20..cpu.cycles step 40).sumOf { it * cpu.getXDuring(it) }
    }

    fun part2(input: List<String>): String {
        val cpu = Cpu()
        readInstructions(input).forEach {
            cpu.execute(it)
        }
        return cpu.draw().joinToString("\n")
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day10_test")
    check(part1(testInput) == 13140)
    check(part2(testInput) == """
        ##..##..##..##..##..##..##..##..##..##..
        ###...###...###...###...###...###...###.
        ####....####....####....####....####....
        #####.....#####.....#####.....#####.....
        ######......######......######......####
        #######.......#######.......#######.....
    """.trimIndent())

    val input = readInput("Day10")
    println(part1(input))
    println(part2(input))
}
