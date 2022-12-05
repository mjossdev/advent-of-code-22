private enum class Hand(val score: Int) {
    ROCK(1), PAPER(2), SCISSORS(3);

    val loser
        get() = when (this) {
            ROCK -> SCISSORS
            PAPER -> ROCK
            SCISSORS -> PAPER
        }

    val winner
        get() = when (this) {
            ROCK -> PAPER
            PAPER -> SCISSORS
            SCISSORS -> ROCK
        }

    fun scoreAgainst(other: Hand) = score + when (other) {
        loser -> Result.WIN
        this -> Result.TIE
        else -> Result.LOSS
    }.score
}

private enum class Result(val score: Int) {
    WIN(6), TIE(3), LOSS(0);
}

fun main() {
    fun String.toHand() = when (this) {
        "A", "X" -> Hand.ROCK
        "B", "Y" -> Hand.PAPER
        "C", "Z" -> Hand.SCISSORS
        else -> throw IllegalArgumentException()
    }

    fun String.toResult() = when (this) {
        "X" -> Result.LOSS
        "Y" -> Result.TIE
        "Z" -> Result.WIN
        else -> throw IllegalArgumentException()
    }

    fun solve(opponentHand: Hand, desiredResult: Result): Hand = when (desiredResult) {
        Result.WIN -> opponentHand.winner
        Result.TIE -> opponentHand
        Result.LOSS -> opponentHand.loser
    }

    fun part1(input: List<String>): Int = input.sumOf {
        val (other, mine) = it.split(' ')
        mine.toHand().scoreAgainst(other.toHand())
    }

    fun part2(input: List<String>): Int = input.sumOf {
        val (left, right) = it.split(' ')
        val opponentHand = left.toHand()
        val desiredResult = right.toResult()
        solve(opponentHand, desiredResult).scoreAgainst(opponentHand)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 12)

    val input = readInput("Day02")
    println(part1(input))
    println(part2(input))
}
