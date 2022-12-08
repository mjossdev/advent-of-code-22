import java.math.BigInteger
import java.security.MessageDigest
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
