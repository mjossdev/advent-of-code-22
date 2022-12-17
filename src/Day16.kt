import kotlin.collections.ArrayDeque
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.Set
import kotlin.collections.any
import kotlin.collections.asSequence
import kotlin.collections.associateBy
import kotlin.collections.drop
import kotlin.collections.emptyList
import kotlin.collections.first
import kotlin.collections.forEach
import kotlin.collections.getValue
import kotlin.collections.hashMapOf
import kotlin.collections.isNotEmpty
import kotlin.collections.last
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.mapValues
import kotlin.collections.maxOfOrNull
import kotlin.collections.minus
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.collections.sortedByDescending
import kotlin.collections.sumOf
import kotlin.collections.toList
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

    class PathFinder {
        private val pathCache = hashMapOf<Pair<Valve, Valve>, List<Valve>>()

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
    }

    class ObjectPool<T> {
        private val cache = hashMapOf<T, T>()

        operator fun get(value: T) = cache.computeIfAbsent(value) { value }
    }

    fun calculateMaxReleasedPressure(valves: Map<String, Valve>, remainingMinutes: Int, numberOfTraversers: Int): Int {
        val pathFinder = PathFinder()
        val setCache = ObjectPool<Set<Valve>>()

        data class TraverserState(val currentValve: Valve, val remainingMinutes: Int)
        data class State(
            val traversers: List<TraverserState>,
            val releasedPressure: Int,
            val remainingValves: Set<Valve>
        )

        val start = valves.getValue("AA")
        val usefulValves = valves.values.asSequence().filter { it.flowRate > 0 }.toSet()
        var states = TraverserState(start, remainingMinutes).let {
            listOf(State(List(numberOfTraversers) { _ -> it }, 0, usefulValves))
        }
        var maxReleasedPressure = 0

        while (states.isNotEmpty()) {
            states = states.asSequence().filter {
                it.remainingValves.isNotEmpty() && it.traversers.any { t -> t.remainingMinutes > 0 }
            }.flatMap { state ->
                val (traversers, releasedPressure, remainingValves) = state
                val orderedTraversers = traversers.sortedByDescending { it.remainingMinutes }
                val next = orderedTraversers.first()
                val rest = orderedTraversers.drop(1)
                remainingValves.map {
                    val path = pathFinder.findPath(next.currentValve, it)
                    if (path.size > next.remainingMinutes) {
                        state.copy(traversers = emptyList())
                    } else {
                        val valve = path.last()
                        val minutes = next.remainingMinutes - path.size
                        val pressure = releasedPressure + valve.flowRate * minutes
                        State(
                            listOf(TraverserState(valve, minutes)) + rest,
                            pressure,
                            setCache[remainingValves - valve]
                        )
                    }
                }
            }.filter {
                it.releasedPressure + (it.traversers.maxOfOrNull { t -> t.remainingMinutes - 1 }
                    ?: 0) * it.remainingValves.sumOf { v -> v.flowRate } > maxReleasedPressure
            }.toList()
            maxReleasedPressure = max(maxReleasedPressure, states.maxOfOrNull { it.releasedPressure } ?: 0)
        }
        return maxReleasedPressure
    }

    fun part1(input: List<String>): Int = calculateMaxReleasedPressure(readValves(input), 30, 1)

    fun part2(input: List<String>): Int = calculateMaxReleasedPressure(readValves(input), 26, 2)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day16_test")
    check(part1(testInput) == 1651)
    check(part2(testInput) == 1707)

    val input = readInput("Day16")
    println(part1(input))
    println(part2(input))
}
