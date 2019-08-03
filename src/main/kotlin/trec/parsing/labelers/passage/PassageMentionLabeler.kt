package trec.parsing.labelers.passage

import edu.unh.cs.treccar_v2.Data
import trec.parsing.labelers.CborClassLabeler
import utils.analyzer.AnalyzerFunctions.AnalyzerType.ANALYZER_ENGLISH_STOPPED
import utils.analyzer.AnalyzerFunctions.createTokenList
import utils.trec.filteredInlinks
import utils.trec.foldOverSection
import utils.trec.outlinks


class PassageMentionLabeler(baseDir: String) :
    CborClassLabeler(baseDir) {

    override val className: String
        get() = "mentions"

    override fun labelPage(page: Data.Page) {
        val pageTokens = createTokenList(page.pageName, ANALYZER_ENGLISH_STOPPED)
            .toHashSet() to "page_mention"

        val inlinkTokens = page.filteredInlinks()
            .flatMap { inlink -> createTokenList(inlink, ANALYZER_ENGLISH_STOPPED) }
            .toHashSet() to "inlink_mention"

        val outlinkTokens = page.outlinks()
            .flatMap { outlink -> createTokenList(outlink, ANALYZER_ENGLISH_STOPPED) }
            .toHashSet() to "outlink_mention"

        val redirectTokens = page.pageMetadata.redirectNames
            .flatMap { outlink -> createTokenList(outlink, ANALYZER_ENGLISH_STOPPED) }
            .toHashSet() to "redirect_mention"

        val disambigTokens = page.pageMetadata.disambiguationNames
            .flatMap { outlink -> createTokenList(outlink, ANALYZER_ENGLISH_STOPPED) }
            .toHashSet() to "disambig_mention"

        val categoryTokens = page.pageMetadata.categoryNames.map { it.replace("Category:", "") }
            .flatMap { createTokenList(it, ANALYZER_ENGLISH_STOPPED) }
            .toHashSet() to "category_mention"


        val tokenSets = listOf(
            pageTokens, inlinkTokens, disambigTokens, redirectTokens, outlinkTokens, categoryTokens
        )

        page.foldOverSection() { path: String, section: Data.Section, paragraphs: List<Data.Paragraph> ->
            val sectionTokens = createTokenList(section.heading, ANALYZER_ENGLISH_STOPPED)
                .toHashSet() to "section_mention"

            val finalSets = tokenSets + listOf(sectionTokens)

            paragraphs.forEach { p ->
                val ptokens = createTokenList(p.textOnly, ANALYZER_ENGLISH_STOPPED).toHashSet()
                finalSets.forEach { (tokenSet, mentionCategory) ->
                    if (tokenSet.intersect(ptokens).isNotEmpty()) {
                        writeIdWithClass(p.paraId, mentionCategory)
                    }

                }
            }
        }

    }
}
