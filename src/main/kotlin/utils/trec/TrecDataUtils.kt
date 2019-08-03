package utils.trec

import edu.unh.cs.treccar_v2.Data

fun Data.Page.paragraphs() =
        flatSectionPathsParagraphs()
            .map { secParagraph -> secParagraph.paragraph  }

fun Data.Page.outlinks() =
        paragraphs()
            .flatMap(Data.Paragraph::getEntitiesOnly)
            .map { entity -> entity.replace(" ", "_").replace("%20", "_").replace("enwiki:", "") }
            .toSet()

fun Data.Page.filteredInlinks() =
        this.pageMetadata.inlinkIds.map { it.replace("enwiki:", "").replace("%20", "_").replace(" ","_") }

private val sectionsToIgnore = setOf(
        "See also", "References", "External links", "Further reading", "Notes"
)

fun getSections(cur: Data.Section): List<String> {
    if (cur.heading in sectionsToIgnore) return emptyList()
    val header = listOf(cur.heading)
    return  if (cur.childSections.isEmpty()) header
            else header + cur.childSections.flatMap { child -> getSections(child) }
}

private fun foldOverSection(f: (String, Data.Section, List<Data.Paragraph>) -> Unit, cur: Data.Section, path: String,
                            useFilter: Boolean = true) {
    if (cur.heading !in sectionsToIgnore) {
        val content = cur.children
            .filterIsInstance<Data.Para>()
            .map { it.paragraph }
            .filter { p -> !useFilter || (p.textOnly.length > 100 &&
                    !p.textOnly.contains(":") && !p.textOnly.contains("•")) }
        if (content.isNotEmpty())
            f(path, cur, content)
        cur.childSections.forEach { child -> foldOverSection(f, child, path + "/" + child.heading, useFilter) }
    }
}

private fun foldOverSectionWithId(f: (String, Data.Section, List<Data.Paragraph>) -> Unit, cur: Data.Section, path: String,
                            useFilter: Boolean = true) {
    if (cur.heading !in sectionsToIgnore) {
        val content = cur.children
            .filterIsInstance<Data.Para>()
            .map { it.paragraph }
            .filter { p -> !useFilter || (p.textOnly.length > 100 &&
                    !p.textOnly.contains(":") && !p.textOnly.contains("•")) }
        if (content.isNotEmpty())
            f(path, cur, content)
        cur.childSections.forEach { child -> foldOverSection(f, child, path + "/" + child.headingId, useFilter) }
    }
}

fun Data.Page.flatSectionIntermediatePaths(): List<Pair<String, String>> {
    val pMap = HashSet<Pair<String, String>>()
    val sections = flatSectionPaths().groupBy { it.first().headingId }
//    val sections = flatSectionPaths().groupBy { it.first().heading }
        .forEach { grouping ->
            val topLevel = grouping.key
            grouping.value.forEach { path ->
                (0 .. path.size).forEach { subIndex ->
                    val sId = (listOf(pageId) + path.subList(0, subIndex).map { it.headingId }).joinToString("/")
                    val sName = (listOf(pageName) + path.subList(0, subIndex).map { it.heading }).joinToString("/")
//                    pMap += (listOf(pageId) + path.subList(0, subIndex).map { it.headingId }).joinToString("/")
                    pMap += sId to sName
//                    pMap += (listOf(pageName) + path.subList(0, subIndex).map { it.heading }).joinToString("/")
                }
            }


        }
    pMap += pageId to pageName
//    pMapString += pageName
    return pMap.sortedBy { it.first }
}

@Suppress("UNCHECKED_CAST")
fun Data.Page.foldOverSection(useFilter: Boolean = true, f: (path: String, section: Data.Section, paragraphs: List<Data.Paragraph>) -> Unit) {
    val abstract = skeleton
        .takeWhile { it is Data.Para } as List<Data.Para>
    val pageSection = Data.Section(pageName, pageId, abstract)
    f(pageName, pageSection, abstract.map { it.paragraph } )
    childSections.forEach { section -> foldOverSection(f, section, pageName + "/" + section.heading, useFilter) }
}

@Suppress("UNCHECKED_CAST")
fun Data.Page.foldOverSectionWithId(useFilter: Boolean = true, f: (path: String, section: Data.Section, paragraphs: List<Data.Paragraph>) -> Unit) {
    val abstract = skeleton
        .takeWhile { it is Data.Para } as List<Data.Para>
    val pageSection = Data.Section(pageId, pageId, abstract)
    f(pageId, pageSection, abstract.map { it.paragraph } )
    childSections.forEach { section -> foldOverSectionWithId(f, section, pageId + "/" + section.heading, useFilter) }
}

fun Data.Page.getSectionLevels() =
    childSections.flatMap { child -> getSections(child) }


fun Data.PageMetadata.filteredCategoryNames() =
        categoryNames
            .map { name -> name.split(":")[1].replace(" ", "_") }

fun Data.Page.abstract() = paragraphs().first()


