fun main() {
    class Valve(val name: String, val flowRate: Int) {
        val connections: MutableList<Valve> = mutableListOf()
        var isClosed = true
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

    fun findPath(start: Valve, end: Valve): List<Valve> {
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
                return path.toList()
            }
            v.connections.asSequence().filter { it !in explored }.forEach {
                explored.add(it)
                parents[it] = v
                queue.addLast(it)
            }
        }
        error("no path found")
    }

    fun pressurePerMinute(path: List<Valve>, remainingMinutes: Int): Int {
        var minutes = remainingMinutes
        var pressure = 0
        for (v in path) {
            if (v.isClosed && v.flowRate > 0) {
                --minutes
                pressure += v.flowRate * minutes
            }
            --minutes
        }
        return pressure / (remainingMinutes - minutes)
    }

    fun part1(input: List<String>): Int {
        val valves = readValves(input)
        val usefulValves = valves.values.filter { it.flowRate > 0 }.toMutableSet()
        var start = valves.getValue("AA")
        var remainingMinutes = 30
        var releasedPressure = 0
        while (remainingMinutes > 0 && usefulValves.isNotEmpty()) {
            val path = usefulValves.asSequence()
                .map { findPath(start, it) }
                .maxBy { pressurePerMinute(it, remainingMinutes) }
            for (v in path) {
                println("Move to valve ${v.name}")
                if (v.isClosed && v.flowRate > 0) {
                    --remainingMinutes
                    println("Open valve ${v.name}")
                    releasedPressure += v.flowRate * remainingMinutes
                    v.isClosed = false
                }
                --remainingMinutes
            }
            // one decrement too much
            ++remainingMinutes
            usefulValves -= path
            start = path.last()
        }
        println(releasedPressure)
        return releasedPressure
    }

//    fun part2(input: List<String>, maxCoordinate: Int): Long

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day16_test")
    check(part1(testInput) == 1651)
//    check(part2(testInput, 20) == 56000011L)

    val input = readInput("Day16")
    println(part1(input))
//    println(part2(input, 4_000_000))
}
