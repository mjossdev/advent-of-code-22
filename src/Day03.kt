fun main() {
    fun Char.getPriority(): Int = if (isLowerCase()) {
        1 + (this - 'a')
    } else {
        27 + (this - 'A')
    }

    data class Rucksack(val compartment1: Set<Char>, val compartment2: Set<Char>) {
        fun commonItem(): Char = compartment1.intersect(compartment2).single()

        fun allItems() = compartment1.union(compartment2)
    }

    fun String.toRucksack(): Rucksack {
        val n = length / 2
        return Rucksack(take(n).toSet(), takeLast(n).toSet())
    }

    fun getBadge(rucksacks: List<Rucksack>): Char {
        val commonItems = rucksacks.first().allItems().toMutableSet()
        rucksacks.drop(1).forEach {
            commonItems.retainAll(it.allItems())
        }
        return commonItems.single()
    }

    fun part1(input: List<String>): Int = input.sumOf { it.toRucksack().commonItem().getPriority() }

    fun part2(input: List<String>): Int = input.map { it.toRucksack() }.chunked(3).sumOf { getBadge(it).getPriority() }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 157)
    check(part2(testInput) == 70)

    val input = readInput("Day03")
    println(part1(input))
    println(part2(input))
}
