package trec.parsing

import edu.unh.cs.treccar_v2.Data
import edu.unh.cs.treccar_v2.read_data.DeserializeData
import trec.parsing.labelers.passage.*
import java.io.File

class PageCborParser() {
    val base = "/home/hcgs/data_science/data"
    val labelers = listOf(
        PassagePositionalLabeler("$base/stuff"),
        PassageMentionLabeler("$base/stuff"),
        PassageLinkLabeler("$base/stuff"),
        PassageContentSizeLabeler("$base/stuff"),
        PassageGramImportanceLabeler("$base/stuff")
    )

    fun parse(cborLoc: String) {
        var counter = 0
        val fStream = File(cborLoc).inputStream().buffered()
        DeserializeData.iterableAnnotations(fStream)
            .take(1000)
            .forEach { page: Data.Page ->
                labelers.forEach { labeler ->
                    labeler.labelPage(page)
                }
            }
    }
}

fun main() {
    val wee = PageCborParser()
    val cborList = listOf(
        "/home/hcgs/data_science/data/test200/test200-train/train.pages.cbor",
        "/home/hcgs/data_science/data/page_cbors/test.pages.cbor",
        "/home/hcgs/data_science/data/page_cbors/train.pages.cbor",
        "/home/hcgs/data_science/data/page_cbors/fold-0-unprocessedAllButBenchmark.cbor"
    )

    cborList.forEach {  path -> wee.parse(path) }

}