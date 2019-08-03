package trec.parsing.labelers.passage

import edu.unh.cs.treccar_v2.Data
import trec.parsing.labelers.CborClassLabeler
import utils.trec.filteredCategoryNames
import utils.trec.filteredInlinks
import utils.trec.foldOverSection

class PassageLinkLabeler(baseDir: String) :
    CborClassLabeler(baseDir) {

    override val className: String
        get() = "links"

    override fun labelPage(page: Data.Page) {
        val inlinks = page.filteredInlinks().toHashSet()

        page.foldOverSection() { path: String, section: Data.Section, paragraphs: List<Data.Paragraph> ->
            val sectionLinkCounts = paragraphs.flatMap { p ->
                p.entitiesOnly.toList()
            }.groupingBy { it }
                .eachCount()

            paragraphs.forEach { p ->

                // Link Counts
                if (p.entitiesOnly.size == 1) {
                    writeIdWithClass(p.paraId, "one_link")
                } else if (p.entitiesOnly.size > 2) {
                    writeIdWithClass(p.paraId, "many_links")
                } else {
                    writeIdWithClass(p.paraId, "no_links")
                }

                // Referential Links
                val linkSet = p.entitiesOnly.toHashSet()
                val overlap = linkSet.intersect(inlinks).size
                if (overlap == 0 && linkSet.isNotEmpty()) {
                    writeIdWithClass(p.paraId, "non_referential_links")
                } else if (overlap >= 1) {
                    writeIdWithClass(p.paraId, "self_referential_links")
                }

                val hasCommonLink = p.entitiesOnly.any { sectionLinkCounts[it]!! > 1 }
                if (hasCommonLink) {
                    writeIdWithClass(p.paraId, "common_section_links")
                }

            }
        }

    }
}
