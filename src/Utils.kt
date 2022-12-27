import java.math.BigInteger
import java.security.MessageDigest
import java.util.EnumMap
import kotlin.io.path.Path
import kotlin.io.path.readLines

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src", "$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

fun <T> List<T>.toPair(): Pair<T, T> {
    check(size == 2)
    return Pair(this[0], this[1])
}

fun <T> Iterable<T>.countUntil(predicate: (T) -> Boolean): Int {
    var count = 0
    for (e in this) {
        ++count
        if (predicate(e)) break
    }
    return count
}

fun <T> lexicographicalCompare(left: List<T>, right: List<T>, comparator: (T, T) -> Int): Int =
    left.zip(right).firstNotNullOfOrNull { (l, r) -> comparator(l, r).takeIf { it != 0 } }
        ?: (left.size compareTo right.size)

fun List<Int>.product() = reduce(Int::times)

inline fun repeat(times: Long, block: (Long) -> Unit) {
    for (i in 0L until times) {
        block(i)
    }
}

private val numberRegex = Regex("""\d+""")

fun String.isNumber() = numberRegex.matches(this)

operator fun <T> List<T>.component6() = this[5]
operator fun <T> List<T>.component7() = this[6]

/**
 * Optimized version for EnumMap
 */
inline fun <reified K: Enum<K>, V, R> Map<K, V>.mapValues(transform: (Map.Entry<K, V>) -> R): Map<K, R> = EnumMap<K, R>(K::class.java).also {
    for (entry in this) {
        it[entry.key] = transform(entry)
    }
}
