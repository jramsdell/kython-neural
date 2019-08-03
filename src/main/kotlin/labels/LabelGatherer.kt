package labels

import java.io.File


class LabelGatherer(baseDir: String) {
    init {
        val labels = ArrayList<List<String>>()
        val base = File(baseDir)

        var counter = 0
        val idSets = getLabelFiles(base)
            .sortedBy { it.name }
            .onEach {
                println("${it.name}: $counter")
                counter += 1
            }
            .map { file ->
                file.readLines()
                    .map(String::trim)
                    .toHashSet()
            }


        val wee = idSets.flatten().toHashSet()
            .mapIndexed { index, s -> s to index  }
            .toMap()

        val out = File("wubba.txt")

        val output = idSets.map { idSet ->
            val vector = wee.map { 0.0 }.toDoubleArray()
            idSet.forEach { key ->
                vector[wee[key]!!] = 1.0
            }
            vector.joinToString(" ")
        }.joinToString("\n")


        out.writeText(output)




        }

    fun getLabelFiles(file: File): List<File> {
        if (file.isDirectory) {
            return file.listFiles()?.flatMap { child -> getLabelFiles(child) }
                ?: emptyList()
        }
        return listOf(file)
    }
}

fun main() {
    val base = "/home/hcgs/data_science/data"
    val gatherer = LabelGatherer("$base/stuff")
}