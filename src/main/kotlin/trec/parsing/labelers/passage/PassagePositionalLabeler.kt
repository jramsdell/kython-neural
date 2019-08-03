package trec.parsing.labelers.passage

import edu.unh.cs.treccar_v2.Data
import trec.parsing.labelers.CborClassLabeler
import utils.trec.foldOverSection

class PassagePositionalLabeler(baseDir: String) :
    CborClassLabeler(baseDir) {


    override val className: String
        get() = "positional_labels"

    override fun labelPage(page: Data.Page) {
        page.foldOverSection() { path: String, section: Data.Section, paragraphs: List<Data.Paragraph> ->
            if (paragraphs.size == 1) {
                writeIdWithClass(paragraphs.first().paraId, "singleton")
            } else if (paragraphs.size > 1) {
                writeIdWithClass(paragraphs.first().paraId, "introduction")
                writeIdWithClass(paragraphs.last().paraId, "conclusion")
                paragraphs.subList(1, paragraphs.size - 1)
                    .forEach { p -> writeIdWithClass(p.paraId, "between") }
            }

            val level = path.count { it == '/' }
            paragraphs.forEach { p ->
                writeIdWithClass(p.paraId, "level_$level")
            }
        }

    }
}
