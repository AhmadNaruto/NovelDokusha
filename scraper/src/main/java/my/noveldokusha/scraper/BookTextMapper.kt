package my.noveldokusha.scraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * Utility object for mapping book text and handling image embeddings.
 */
object BookTextMapper {

    /**
     * Represents an image element with its path and vertical position.
     * @param path The image source path
     * @param yRel The vertical position relative to text (0.0-2.0 range typically)
     */
    data class ImgEntry(
        val path: String,
        val yRel: Float
    ) {
        companion object {
            private const val XML_TAG = "img"
            private const val ATTR_SRC = "src"
            private const val ATTR_YREL = "yrel"
            private const val YREL_FORMAT = "%.2f"

            /**
             * Parses an XML string representation of an image entry.
             * Supports both v0 (deprecated) and v1 (current) formats.
             */
            fun fromXmlString(xml: String): ImgEntry? =
                fromXmlStringV1(xml) ?: fromXmlStringV0(xml)

            private fun fromXmlStringV1(xml: String): ImgEntry? =
                Jsoup.parse(xml).selectFirst("$XML_TAG[$ATTR_SRC]")?.let { element ->
                    val path = element.attr(ATTR_SRC).takeIf { it.isNotEmpty() } ?: return null
                    val yRel = element.attr(ATTR_YREL).toFloatOrNull() ?: return null
                    ImgEntry(path, yRel)
                }

            private fun fromXmlStringV0(xml: String): ImgEntry? {
                val xmlFormRegex = """^\W*<img .*>.+</img>\W*$""".toRegex()
                if (!xml.matches(xmlFormRegex)) return null

                return xml.parseXmlDocument()?.selectFirstTag(XML_TAG)?.let { element ->
                    val path = element.textContent.takeIf { !it.isNullOrEmpty() } ?: return null
                    val yRel = element.getAttributeValue(ATTR_YREL)?.toFloatOrNull() ?: return null
                    ImgEntry(path, yRel)
                }
            }
        }

        /**
         * Converts this image entry to XML string format (v1).
         */
        fun toXmlString(): String = buildString {
            append("<$XML_TAG $ATTR_SRC=\"$path\" $ATTR_YREL=\"${YREL_FORMAT.format(yRel)}\">")
        }
    }
}

// Extension functions for XML parsing
internal fun String.parseXmlDocument() = reader().use { reader ->
    runCatching {
        javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(org.xml.sax.InputSource(reader))
    }.getOrNull()
}

internal fun org.w3c.dom.Document.selectFirstTag(tag: String) =
    getElementsByTagName(tag).item(0)

internal fun org.w3c.dom.Node.getAttributeValue(attribute: String) =
    attributes?.getNamedItem(attribute)?.textContent
