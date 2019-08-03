package trec.parsing.labelers.passage

import edu.unh.cs.treccar_v2.Data
import trec.parsing.labelers.CborClassLabeler
import utils.trec.filteredCategoryNames
import utils.trec.foldOverSection

class PassagePageTopicLabeler(baseDir: String) :
    CborClassLabeler(baseDir) {

    override val className: String
        get() = "page_topic"

    override fun labelPage(page: Data.Page) {
        val topics = page.pageMetadata.filteredCategoryNames()
        page.foldOverSection() { path: String, section: Data.Section, paragraphs: List<Data.Paragraph> ->
            paragraphs.forEach { p ->
                topics.forEach { topic ->
                    writeId(p.paraId + "_" + page.pageId, topic)
                }

            }
        }

    }
}
