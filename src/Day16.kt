import kotlin.math.max

fun main() {
    class Valve(val name: String, val flowRate: Int) {
        val connections: MutableList<Valve> = mutableListOf()
    }

    val valveRegex = Regex("""Valve (\S+) has flow rate=(\d+); tunnels? leads? to valves? (.*)""")

    fun readValves(input: List<String>): Map<String, Valve> {
        val valvesWithConnections = input.map {
            val (name, flowRate, connections) = valveRegex.matchEntire(it)!!.destructured
            Valve(name, flowRate.toInt()) to connections.split(", ")
        }.associateBy { it.first.name }
        valvesWithConnections.values.forEach { (valve, connections) ->
            valve.connections.addAll(connections.map { valvesWithConnections.getValue(it).first })
        }
        return valvesWithConnections.mapValues { it.value.first }
    }

    val pathCache = mutableMapOf<Pair<Valve, Valve>, List<Valve>>()

    fun findPath(start: Valve, end: Valve): List<Valve> = pathCache.computeIfAbsent(start to end) {
        val queue = ArrayDeque<Valve>()
        val explored = mutableSetOf(start)
        val parents = mutableMapOf<Valve, Valve>()
        queue.addLast(start)
        while (queue.isNotEmpty()) {
            val v = queue.removeFirst()
            if (v == end) {
                val path = ArrayDeque<Valve>()
                var current: Valve? = v
                while (current != null) {
                    path.addFirst(current)
                    current = parents[current]
                }
                return@computeIfAbsent path.toList()
            }
            v.connections.asSequence().filter { it !in explored }.forEach {
                explored.add(it)
                parents[it] = v
                queue.addLast(it)
            }
        }
        error("no path found")
    }

    data class State(val currentValve: Valve, val remainingMinutes: Int, val pressure: Int, val remainingValves: Set<Valve>)

    fun part1(input: List<String>): Int {
        val valves = readValves(input)
        val usefulValves = valves.values.filter { it.flowRate > 0 }.toSet()
        var states = listOf(State(valves.getValue("AA"), 30, 0, usefulValves))
        var maxReleasedPressure = 0
        while (states.isNotEmpty()) {
            states = states.flatMap { state ->
                val (current, remainingMinutes, releasedPressure, remainingValves) = state
                remainingValves.map {
                    val path = findPath(current, it)
                    if (path.size > remainingMinutes) {
                        state.copy(remainingMinutes = 0)
                    } else {
                        val valve = path.last()
                        val minutes = remainingMinutes - path.size
                        val pressure = releasedPressure + valve.flowRate * minutes
                        State(valve, minutes, pressure, remainingValves - valve)
                    }
                }
            }
            maxReleasedPressure = max(maxReleasedPressure, states.maxOf { it.pressure })
            states = states.filter { it.remainingMinutes > 0 && it.remainingValves.isNotEmpty() }
        }
        return maxReleasedPressure
    }

//    fun part2(input: List<String>): Int

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day16_test")
    check(part1(testInput) == 1651)
//    check(part2(testInput) == 1707)

    val input = readInput("Day16")
    println(part1(input))
//    println(part2(input))
}
