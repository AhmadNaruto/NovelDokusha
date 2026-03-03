package my.noveldokusha.scraper

import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * Utility object for extracting text from HTML nodes.
 * Handles paragraphs, line breaks, and embedded images.
 */
object TextExtractor {

    private const val PARAGRAPH_SEPARATOR = "\n\n"
    private const val LINE_BREAK = "\n"
    private const val IMAGE_YREL_DEFAULT = 1.45f

    /**
     * Extracts text content from an HTML node.
     * Paragraphs are separated by double newlines.
     * @deprecated Use [extract] instead
     */
    @Deprecated("Use extract", ReplaceWith("extract(node)"))
    fun get(node: Node?): String = extract(node)

    /**
     * Extracts text content from an HTML node.
     * Paragraphs are separated by double newlines.
     */
    fun extract(node: Node?): String {
        val children = node?.childNodes().orEmpty()
        if (children.isEmpty()) return ""

        return children.joinToString("") { child ->
            when {
                child.isParagraph() -> extractParagraph(child)
                child.isLineBreak() -> LINE_BREAK
                child.isHorizontalRule() -> PARAGRAPH_SEPARATOR
                child.isImage() -> embedImage(child)
                child is TextNode -> child.text().trim()
                else -> extractNodeText(child)
            }
        }
    }

    private fun extractParagraph(node: Node): String {
        val text = node.childNodes().joinToString("") { child ->
            when {
                child.isLineBreak() -> LINE_BREAK
                child is TextNode -> child.text()
                child.isImage() -> embedImage(child)
                else -> extractNodeText(child)
            }
        }.trim()

        return text.takeIf { it.isNotEmpty() }?.plus(PARAGRAPH_SEPARATOR).orEmpty()
    }

    private fun extractNodeText(node: Node): String {
        val children = node.childNodes()
        if (children.isEmpty()) return ""

        return children.joinToString("") { child ->
            when {
                child.isParagraph() -> extractParagraph(child)
                child.isLineBreak() -> LINE_BREAK
                child.isHorizontalRule() -> PARAGRAPH_SEPARATOR
                child.isImage() -> embedImage(child)
                child is TextNode -> child.text().trim()
                else -> extractNodeText(child)
            }
        }
    }

    private fun embedImage(node: Node): String {
        val src = (node as? org.jsoup.nodes.Element)?.attr("src").orEmpty()
        if (src.isEmpty()) return ""

        val imageEntry = BookTextMapper.ImgEntry(
            path = src,
            yRel = IMAGE_YREL_DEFAULT
        )
        return "\n\n${imageEntry.toXmlString()}\n\n"
    }

    // Extension predicates for node type checking
    private fun Node.isParagraph() = nodeName() == "p"
    private fun Node.isLineBreak() = nodeName() == "br"
    private fun Node.isHorizontalRule() = nodeName() == "hr"
    private fun Node.isImage() = nodeName() == "img"
}
