package trec.parsing.labelers.passage

import edu.unh.cs.treccar_v2.Data
import trec.parsing.labelers.CborClassLabeler
import utils.analyzer.AnalyzerFunctions
import utils.trec.filteredCategoryNames
import utils.trec.filteredInlinks
import utils.trec.foldOverSection
import utils.trec.outlinks

typealias nGramMap =  HashMap<Int, HashMap<String, Int>>
typealias nGramListMap =  HashMap<Int, List<String>>

class PassageGramImportanceLabeler(baseDir: String) :
    CborClassLabeler(baseDir) {

    override val className: String
        get() = "passage_gram_importance"

    override fun labelPage(page: Data.Page) {
        val gramCounter: nGramMap = nGramMap()
        val allPassages = ArrayList<Pair<String, nGramListMap>>()
        (2..4).forEach { nGramSize ->
            gramCounter[nGramSize] = HashMap<String, Int>()
        }

        page.foldOverSection() { path: String, section: Data.Section, paragraphs: List<Data.Paragraph> ->
            paragraphs.forEach { p ->
                val nListMap: nGramListMap = nGramListMap()

                (2..4).forEach { nGramSize ->
                    val curList = ArrayList<String>()
                    val curCounter = gramCounter[nGramSize]!!

                    val nGrams = AnalyzerFunctions.createTokenList(
                        p.textOnly,
                        AnalyzerFunctions.AnalyzerType.ANALYZER_ENGLISH_STOPPED
                    )
                        .windowed(nGramSize, 1)
                        .map { (g1, g2) -> g1 + "_" + g2 }

                    nGrams
                        .forEach { nGram ->
                            curList.add(nGram)
                            if (nGram !in curCounter)
                                curCounter[nGram] = 1
                            else
                                curCounter[nGram] = curCounter[nGram]!! + 1
                        }
                    nListMap[nGramSize] = curList
                }
                allPassages.add(p.paraId to nListMap)
            }
        }

        (2..4).forEach { nGramSize ->
            val curCounter = gramCounter[nGramSize]!!

            val total = curCounter.values.sum()
            val gramDist = curCounter.map { (k, v) -> k to v.toDouble() / total }
                .toMap()

            val scoredPassages = allPassages.map { (pid, gramLists) ->
                val grams = gramLists[nGramSize]!!
                val score = grams.sumByDouble { gram ->
                    gramDist[gram]!!
                }

                pid to score / grams.size
            }

            scoredPassages.sortedByDescending { (_, score) -> score }
                .take(3)
                .forEach { (pid, _) ->
                    writeIdWithClass(pid, "passage_${nGramSize}gram_importance")
                }

            scoredPassages.sortedBy { (_, score) -> score }
                .take(3)
                .forEach { (pid, _) ->
                    writeIdWithClass(pid, "passage_${nGramSize}gram_unimportance")
                }

//            val midpoint = scoredPassages.size / 2
//            (0 until 2).forEach { offset ->
//                val p = scoredPassages[midpoint - offset + 1]
//                writeIdWithClass(p.first, "passage_${nGramSize}gram_middle_importance")
//            }

//            scoredPassages.sortedByDescending { (_, score) -> score }
//                .take(4)
//                .forEach { (pid, _) ->
//                    writeIdWithClass(pid, "passage_${nGramSize}gram_unimportance")
//                }

        }
    }
}
