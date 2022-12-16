import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class AdaptablePriorityQueue<K : Comparable<K>, V> {
    private val heap = mutableListOf<EntryImpl>()
    val size get() = heap.size

    fun insert(key: K, value: V): Entry<K, V> {
        val entry = EntryImpl(key, value, size + 1)
        heap.add(entry)
        upHeap(size)
        return entry
    }

    private fun index(virtualIndex: Int) = virtualIndex - 1

    private tailrec fun upHeap(i: Int) {
        if (i == 1) return
        val entry = heap[index(i)]
        val parent = heap[index(i / 2)]
        if (entry.key < parent.key) {
            swap(i, i / 2)
            upHeap(i / 2)
        }
    }

    private fun swap(i: Int, j: Int) {
        val temp = heap[index(i)]
        heap[index(i)] = heap[index(j)]
        heap[index(j)] = temp
        heap[index(i)].index = i
        heap[index(j)].index = j
    }

    fun replaceKey(entry: Entry<K, V>, newKey: K): K {
        checkEntry(entry)
        val oldKey = entry.key
        entry.key = newKey
        val cmp = newKey compareTo oldKey
        if (cmp < 0) {
            upHeap(entry.index)
        } else if (cmp > 0) {
            downHeap(entry.index)
        }
        return oldKey
    }

    fun removeMin(): Entry<K, V> = remove(heap[index(1)])

    private tailrec fun downHeap(i: Int) {
        if (i * 2 > size) {
            return
        }
        val entry = heap[index(i)]
        val leftChild = heap[index(i * 2)]
        val rightChild = heap.getOrNull(index(i * 2 + 1))
        val smallerChild = if (rightChild == null || leftChild.key <= rightChild.key) {
            leftChild
        } else {
            rightChild
        }
        if (entry.key >= smallerChild.key) {
            swap(i, smallerChild.index)
            downHeap(entry.index)
        }
    }

    fun remove(entry: Entry<K, V>): Entry<K, V> {
        checkEntry(entry)
        val i = entry.index
        swap(i, size)
        heap.removeAt(index(size))
        if (isEmpty()) {
            return entry
        }
        val replacement = heap[index(i)]
        if (replacement.key < entry.key) {
            upHeap(i)
        } else {
            downHeap(i)
        }
        return entry
    }

    fun isEmpty() = size == 0

    @OptIn(ExperimentalContracts::class)
    private fun checkEntry(entry: Entry<K, V>) {
        contract {
            returns() implies (entry is EntryImpl)
        }
        check(entry is EntryImpl && entry.index <= heap.size && heap[index(entry.index)] == entry) { "entry does not belong to queue" }
    }

    interface Entry<K, V> {
        val key: K
        val value: V

        operator fun component1() = key
        operator fun component2() = value
    }

    private inner class EntryImpl(override var key: K, override var value: V, var index: Int) : Entry<K, V>
}
