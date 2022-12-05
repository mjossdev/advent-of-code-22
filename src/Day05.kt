fun main() {
    data class Move(val n: Int, val source: Int, val target: Int)

    fun String.toMove(): Move {
        val (n, source, target) = Regex("""move (\d+) from (\d+) to (\d+)""")
            .matchEntire(this)!!
            .groupValues
            .drop(1)
            .map { it.toInt() }
        return Move(n, source - 1, target - 1);
    }

    fun readStacks(input: List<String>): Array<ArrayDeque<Char>> {
        val size = input.last().split(Regex("""\s+""")).last().toInt()
        val stackData = input.dropLast(1)
        val stacks = Array(size) { ArrayDeque<Char>() }
        for (line in stackData) {
            for (i in 0..line.length / 4) {
                if (line[i * 4] == '[') {
                    stacks[i].addFirst(line[i * 4 + 1])
                }
            }
        }
        return stacks
    }

    fun part1(input: List<String>): String {
        val divider = input.indexOf("")
        val stacks = readStacks(input.take(divider))
        val moves = input.drop(divider + 1).map { it.toMove() }
        for ((n, source, target) in moves) {
            repeat(n) {
                stacks[target].addLast(stacks[source].removeLast())
            }
        }
        return stacks.joinToString("") { it.last().toString() }
    }

    fun part2(input: List<String>): String {
        val divider = input.indexOf("")
        val stacks = readStacks(input.take(divider))
        val moves = input.drop(divider + 1).map { it.toMove() }
        for ((n, source, target) in moves) {
            val elements = ArrayDeque<Char>()
            repeat(n) {
                elements.addFirst(stacks[source].removeLast())
            }
            stacks[target].addAll(elements)
        }
        return stacks.joinToString("") { it.last().toString() }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day05_test")
    check(part1(testInput) == "CMZ")
    check(part2(testInput) == "MCD")

    val input = readInput("Day05")
    println(part1(input))
    println(part2(input))
}
