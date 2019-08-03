package trec.parsing.labelers

import edu.unh.cs.treccar_v2.Data
import trec.parsing.containers.CborClassLabelType
import java.io.File

abstract class CborClassLabeler(val baseDir: String) {
    open val labelType: CborClassLabelType = CborClassLabelType.LABEL_PARAGRAPH
    open val className: String = "abstract"

    fun getPath(name: String) = "$baseDir/${labelType.label}/$className/${name.take(3)}/"
    val basePath = "$baseDir/${labelType.label}/$className/"
    var first: Boolean = true
    val initialized = HashSet<String>()

    fun writeId(id: String, category: String? = null) {
        val path = if (category != null) getPath(category) else basePath

        if (!File(path).exists())
            File(path).mkdirs()

        File(path + (category ?: className))
            .also { f -> if (first) f.writeText("")}
            .appendText(id + "\n")
        first = false
    }

    fun writeIdWithClass(id: String, newClassName: String) {
        val path = "$basePath/$newClassName/"
        if (!File(path).exists())
            File(path).mkdirs()

        File(path + newClassName)
            .also { f -> if (initialized.add(newClassName)) f.writeText("")}
            .appendText(id + "\n")
        first = false
    }


    abstract fun labelPage(page: Data.Page)
}