import java.util.*
import kotlin.math.max

private enum class Material {
    ORE, CLAY, OBSIDIAN;
}

fun main() {
    data class Blueprint(
        val id: Int,
        val costs: Map<Material, Map<Material, Int>>,
        val geodeCosts: Map<Material, Int>
    )

    fun Map<Material, Int>.meets(cost: Map<Material, Int>) =
        cost.all { (material, amount) -> this.getValue(material) >= amount }

    fun Map<Material, Int>.subtract(cost: Map<Material, Int>) =
        mapValues { (material, amount) -> amount - cost.getValue(material) }

    fun Map<Material, Int>.plus(production: Map<Material, Int>) =
        mapValues { (material, amount) -> amount + production.getValue(material) }

    fun Map<Material, Int>.increment(material: Material) =
        EnumMap(this).apply { this[material] = this.getValue(material) + 1 }

    fun createMap(ore: Int = 0, clay: Int = 0, obsidian: Int = 0) = EnumMap<Material, Int>(Material::class.java).apply {
        this[Material.ORE] = ore
        this[Material.CLAY] = clay
        this[Material.OBSIDIAN] = obsidian
    }

    val blueprintRegex =
        Regex("""Blueprint (\d+): Each ore robot costs (\d+) ore\. Each clay robot costs (\d+) ore\. Each obsidian robot costs (\d+) ore and (\d+) clay\. Each geode robot costs (\d+) ore and (\d+) obsidian\.""")

    fun readBlueprints(input: List<String>): List<Blueprint> = input.map { line ->
        blueprintRegex.matchEntire(line)!!.groupValues.drop(1).map { it.toInt() }
            .let { (id, oreOre, clayOre, obsidianOre, obsidianClay, geodeOre, geodeObsidian) ->
                Blueprint(
                    id,
                    EnumMap(
                        mapOf(
                            Material.ORE to createMap(ore = oreOre),
                            Material.CLAY to createMap(ore = clayOre),
                            Material.OBSIDIAN to createMap(ore = obsidianOre, clay = obsidianClay)
                        )
                    ),
                    createMap(ore = geodeOre, obsidian = geodeObsidian)
                )
            }
    }

    fun calculateQualityLevel(blueprint: Blueprint, minutes: Int): Int {
        data class State(val stock: Map<Material, Int>, val production: Map<Material, Int>, val geodes: Int)

        var maxGeodes = 0
        var states = setOf(State(createMap(), createMap(ore = 1), 0))
        repeat(minutes) { m ->
            val remainingMinutes = minutes - m
            states = states.asSequence().flatMap { (stock, production, geodes) ->
                val newStock = stock.plus(production)
                sequence {
                    if (stock.meets(blueprint.geodeCosts)) {
                        yield(State(newStock.subtract(blueprint.geodeCosts), production, geodes + remainingMinutes - 1))
                        return@sequence
                    }
                    for ((material, costs) in blueprint.costs) {
                        val maxUsage = remainingMinutes * max(
                            blueprint.costs.values.maxOf { it.getValue(material) },
                            blueprint.geodeCosts.getValue(material)
                        )
                        if (newStock.getValue(material) < maxUsage && stock.meets(costs)) {
                            yield(State(newStock.subtract(costs), production.increment(material), geodes))
                        }
                    }
                    yield(State(newStock, production, geodes))
                }
            }.filter { it.geodes + (0 ..remainingMinutes - 2).sum() > maxGeodes }
                .toSet()
            states.maxOfOrNull { it.geodes }?.let {
                maxGeodes = max(it, maxGeodes)
            }
        }
        println("blueprint ${blueprint.id} done")
        return maxGeodes * blueprint.id
    }

    fun part1(input: List<String>): Int {
        val blueprints = readBlueprints(input)
        return blueprints.parallelStream().mapToInt { calculateQualityLevel(it, 24) }.sum()
    }

//    fun part2(input: List<String>): Int

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day19_test")
    check(part1(testInput) == 33)
//    check(part2(testInput) == 58)

    val input = readInput("Day19")
    println(part1(input))
//    println(part2(input))
}
