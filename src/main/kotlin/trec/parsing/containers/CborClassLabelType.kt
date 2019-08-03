package trec.parsing.containers

enum class CborClassLabelType(val label: String) {
    LABEL_PAGE("page"),
    LABEL_PARAGRAPH("paragraph"),
    LABEL_PARAGRAPH_ENTITY("paragraph_entity"),
    LABEL_SECTION("section"),
    LABEL_SENTENCE("sentence")
}