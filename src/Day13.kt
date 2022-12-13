private sealed interface Packet

private data class ListPacket(val packets: List<Packet>) : Packet

private data class IntPacket(val value: Int) : Packet

private class PacketParser private constructor(data: String) {
    private val iterator = data.iterator()
    private var current: Char = 'd'

    fun parse(): Packet {
        next()
        val packet = parsePacket()
        check(!hasNext())
        return packet
    }

    private fun parsePacket(): Packet {
        return if (current == '[') {
            parseListPacket()
        } else {
            parseIntPacket()
        }
    }


    private fun parseIntPacket(): IntPacket {
        check(current.isDigit())
        var value = 0
        while (hasNext() && current.isDigit()) {
            value = value * 10 + current.digitToInt()
            next()
        }
        return IntPacket(value)
    }

    private fun parseListPacket(): ListPacket {
        check(current == '[')
        next()
        val list = if (current == ']') {
            emptyList()
        } else {
            buildList {
                add(parsePacket())
                while (current == ',') {
                    next()
                    add(parsePacket())
                }
            }
        }
        check(current == ']')
        if (hasNext()) {
            next()
        }
        return ListPacket(list)
    }

    private fun next() {
        current = iterator.nextChar()
    }

    private fun hasNext() = iterator.hasNext()

    companion object {
        fun parse(data: String) = PacketParser(data).parse()
    }
}

fun main() {
    fun compare(left: Packet, right: Packet): Int = when (left) {
        is ListPacket -> {
            val list = when (right) {
                is ListPacket -> right.packets
                is IntPacket -> listOf(right)
            }
            lexicographicalCompare(left.packets, list, ::compare)
        }

        is IntPacket -> {
            when (right) {
                is IntPacket -> left.value compareTo right.value
                is ListPacket -> lexicographicalCompare(listOf(left), right.packets, ::compare)
            }
        }
    }

    operator fun Packet.compareTo(other: Packet) = compare(this, other)

    fun part1(input: List<String>): Int = input.chunked(3).mapIndexed { index, (left, right) ->
        if (PacketParser.parse(left) <= PacketParser.parse(right)) index + 1 else 0
    }.sum()

    fun part2(input: List<String>): Int {
        val dividers = listOf(PacketParser.parse("[[2]]"), PacketParser.parse("[[6]]"))
        val packets = (input.filter { it.isNotBlank() }.map(PacketParser::parse) + dividers).sortedWith(::compare)
        return dividers.map { packets.indexOf(it) + 1 }.product()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day13_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 140)

    val input = readInput("Day13")
    println(part1(input))
    println(part2(input))
}
