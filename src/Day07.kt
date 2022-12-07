private sealed interface FileSystemObject {
    val name: String
    val parent: Directory
    val size: Int
}

private sealed interface Directory : FileSystemObject {
    val children: MutableList<FileSystemObject>
    fun getChild(name: String): FileSystemObject
}

private class File (override val name: String, override val parent: Directory, override val size: Int) : FileSystemObject

private class DirectoryImpl(override val name: String, parent: Directory?): Directory {
    override val parent: Directory = parent ?: this
    override val children: MutableList<FileSystemObject> = mutableListOf()

    override val size: Int
        get() = children.sumOf { it.size }

    override fun getChild(name: String): FileSystemObject = children.single { it.name == name }
}

fun main() {
    fun readFileSystem(input: List<String>): Directory {
        val root = DirectoryImpl("/", null)
        var currentDirectory: Directory = root
        for (line in input.drop(1)) {
            when {
                line.startsWith("$ cd") -> {
                    val dirName = line.split(' ').last()
                    currentDirectory = if (dirName == "..") {
                        currentDirectory.parent
                    } else {
                        currentDirectory.getChild(dirName) as Directory
                    }
                }
                line.startsWith("dir") -> {
                    val dirName = line.split(' ').last()
                    currentDirectory.children.add(DirectoryImpl(dirName, currentDirectory))
                }
                line.contains(Regex("""^\d+""")) -> {
                    val (size, name) = line.split(' ')
                    currentDirectory.children.add(File(name, currentDirectory, size.toInt()))
                }
            }
        }
        return root
    }

    fun sumDirectorySizes(root: Directory, maxSize: Int): Int {
        val childrenSize = root.children.filterIsInstance<Directory>().sumOf { sumDirectorySizes(it, maxSize) }
        val size = root.size
        return childrenSize + if (size > maxSize) 0 else size
    }

    fun findSmallestDirectory(root: Directory, minSize: Int): Directory? {
        val size = root.size
        if (size < minSize) return null
        val smallestChild = root.children.filterIsInstance<Directory>().mapNotNull { findSmallestDirectory(it, minSize) }.minByOrNull { it.size }
        return smallestChild ?: root
    }

    fun part1(input: List<String>): Int {
        val tree = readFileSystem(input)
        return sumDirectorySizes(tree, 100000)
    }
    fun part2(input: List<String>): Int {
        val tree = readFileSystem(input)
        val maxSpace = 70000000
        val neededSpace = 30000000
        val availableSpace = maxSpace - tree.size
        val spaceToFree = neededSpace - availableSpace
        return findSmallestDirectory(tree, spaceToFree)!!.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 95437)
    check(part2(testInput) == 24933642)

    val input = readInput("Day07")
    println(part1(input))
    println(part2(input))
}
