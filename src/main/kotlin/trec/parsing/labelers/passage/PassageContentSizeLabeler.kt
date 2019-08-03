package trec.parsing.labelers.passage

import edu.unh.cs.treccar_v2.Data
import trec.parsing.labelers.CborClassLabeler
import utils.analyzer.AnalyzerFunctions
import utils.trec.foldOverSection

class PassageContentSizeLabeler(baseDir: String) :
    CborClassLabeler(baseDir) {


    override val className: String
        get() = "content_size"

    override fun labelPage(page: Data.Page) {
        page.foldOverSection() { path: String, section: Data.Section, paragraphs: List<Data.Paragraph> ->
            paragraphs.forEach { p ->


                var nounCount = 0
                var ignoreNextNoun = true
                p.textOnly.forEach {
                    if (!ignoreNextNoun && it.isUpperCase() && it.isLetter()) {
                        nounCount += 1
                    }
                    if (ignoreNextNoun) { ignoreNextNoun = false }
                    if (it == '.') { ignoreNextNoun = true }
                }

                val pUniqLength = AnalyzerFunctions.createTokenList(p.textOnly, AnalyzerFunctions.AnalyzerType.ANALYZER_ENGLISH_STOPPED)
                    .toHashSet()
                    .size

                val pLength = AnalyzerFunctions.createTokenList(p.textOnly, AnalyzerFunctions.AnalyzerType.ANALYZER_ENGLISH_STOPPED)
                    .size

                val nPeriods = p.textOnly.count { it == '.' }

                (1 until 5).forEach { stepSize ->

                    if ((stepSize - 1) * 5 < pUniqLength && pUniqLength <= stepSize * 5) {
                        writeIdWithClass(p.paraId, "uniq_${stepSize * 5}_terms")
                    }

                    if ((stepSize - 1) * 5 < pLength && pLength <= stepSize * 5) {
                        writeIdWithClass(p.paraId, "${stepSize * 5}_terms")
                    }

                    if ((stepSize - 1) * 2 < nounCount && nounCount <= stepSize * 2) {
                        writeIdWithClass(p.paraId, "${stepSize * 2}_nouns")
                    }

                    if ((stepSize - 1) * 1 < nPeriods && nPeriods <= stepSize * 1) {
                        writeIdWithClass(p.paraId, "${stepSize * 1}_periods")
                    }
                }

                if (p.textOnly.any { it.isDigit() }) {
                    writeIdWithClass(p.paraId, "contains_number")
                }
            }

        }

    }
}
