package my.noveldokusha.scraper

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements

/**
 * Converts an HTML Element to clean, well-formatted plain text.
 * 
 * Features:
 * - Normalizes whitespace (collapses multiple spaces/newlines)
 * - Preserves paragraph structure with double newlines
 * - Handles line breaks, horizontal rules, and lists
 * - Removes script and style tags completely
 * - Trims leading/trailing whitespace
 * - Validates and sanitizes output
 * 
 * @param trim Whether to trim the final result (default: true)
 * @return Clean, normalized plain text
 */
fun Element.toText(trim: Boolean = true): String {
    val result = buildString {
        extractText(this@toText, this)
    }
    
    return if (trim) {
        result.normalizeWhitespace().trim()
    } else {
        result.normalizeWhitespace()
    }
}

/**
 * Converts an HTML Element to Markdown format.
 * 
 * Features:
 * - Converts headings (# h1 to ###### h6)
 * - Preserves paragraphs with double newlines
 * - Converts lists (unordered with -, ordered with numbers)
 * - Converts links to [text](url) format
 * - Converts images to ![alt](url) format
 * - Handles bold (**text**) and italic (*text*) emphasis
 * - Converts blockquotes (> text)
 * - Converts code blocks and inline code
 * - Handles horizontal rules (---)
 * - Normalizes whitespace and validates output
 * 
 * @param trim Whether to trim the final result (default: true)
 * @return Well-formatted Markdown text
 */
fun Element.toMarkdown(trim: Boolean = true): String {
    val result = buildString {
        convertToMarkdown(this@toMarkdown, this)
    }
    
    return if (trim) {
        result.normalizeWhitespace().trim()
    } else {
        result.normalizeWhitespace()
    }
}

/**
 * Converts a collection of Elements to plain text.
 */
fun Elements.toText(trim: Boolean = true): String = joinToString("\n\n") { it.toText(trim) }

/**
 * Converts a collection of Elements to Markdown.
 */
fun Elements.toMarkdown(trim: Boolean = true): String = joinToString("\n\n") { it.toMarkdown(trim) }

// ============================================================================
// Internal Implementation
// ============================================================================

/**
 * Recursively extract text content with proper paragraph structure.
 */
private fun extractText(node: Node, builder: StringBuilder) {
    when {
        node is TextNode -> {
            val text = node.text()
            if (text.isNotBlank()) {
                builder.append(text)
            }
        }
        node is Element -> {
            val tagName = node.tagName().lowercase()
            
            // Skip script and style tags entirely
            if (tagName == "script" || tagName == "style") {
                return
            }
            
            // Handle different HTML elements
            when (tagName) {
                "p" -> {
                    val text = node.extractInnerText()
                    if (text.isNotBlank()) {
                        builder.append("\n\n")
                        builder.append(text)
                        builder.append("\n\n")
                    }
                }
                "br" -> {
                    builder.append("\n")
                }
                "hr" -> {
                    builder.append("\n\n")
                }
                "div", "article", "section", "main", "content" -> {
                    builder.append("\n\n")
                    node.childNodes().forEach { extractText(it, builder) }
                    builder.append("\n\n")
                }
                "h1", "h2", "h3", "h4", "h5", "h6" -> {
                    val text = node.extractInnerText()
                    if (text.isNotBlank()) {
                        builder.append("\n\n")
                        builder.append(text)
                        builder.append("\n\n")
                    }
                }
                "li" -> {
                    val text = node.extractInnerText()
                    if (text.isNotBlank()) {
                        builder.append("\n")
                        builder.append(text)
                    }
                }
                "ul", "ol" -> {
                    builder.append("\n")
                    node.childNodes().forEach { extractText(it, builder) }
                    builder.append("\n")
                }
                "blockquote" -> {
                    builder.append("\n\n")
                    node.childNodes().forEach { extractText(it, builder) }
                    builder.append("\n\n")
                }
                else -> {
                    // For other elements, just process children
                    node.childNodes().forEach { extractText(it, builder) }
                }
            }
        }
        else -> {
            // For other node types, process children
            node.childNodes().forEach { extractText(it, builder) }
        }
    }
}

/**
 * Extract inner text from an element with proper whitespace handling.
 */
private fun Element.extractInnerText(): String {
    return buildString {
        childNodes().forEach { node ->
            when (node) {
                is TextNode -> {
                    val text = node.text()
                    if (text.isNotBlank()) {
                        append(text)
                    }
                }
                is Element -> {
                    val tagName = node.tagName().lowercase()
                    when (tagName) {
                        "br" -> append("\n")
                        "img" -> {
                            val alt = node.attr("alt")
                            if (alt.isNotBlank()) {
                                append(alt)
                            }
                        }
                        else -> append(node.extractInnerText())
                    }
                }
            }
        }
    }.trim()
}

/**
 * Recursively convert HTML to Markdown format.
 */
private fun convertToMarkdown(node: Node, builder: StringBuilder) {
    when {
        node is TextNode -> {
            val text = node.text()
            if (text.isNotBlank()) {
                builder.append(text)
            }
        }
        node is Element -> {
            val tagName = node.tagName().lowercase()
            
            // Skip script and style
            if (tagName == "script" || tagName == "style" || tagName == "noscript") {
                return
            }
            
            when (tagName) {
                // Headings
                "h1", "h2", "h3", "h4", "h5", "h6" -> {
                    val level = tagName.substring(1).toInt()
                    builder.append("\n\n")
                    repeat(level) { builder.append("#") }
                    builder.append(" ")
                    node.childNodes().forEach { convertToMarkdown(it, builder) }
                    builder.append("\n\n")
                }
                
                // Paragraphs
                "p" -> {
                    builder.append("\n\n")
                    node.childNodes().forEach { convertToMarkdown(it, builder) }
                    builder.append("\n\n")
                }
                
                // Line break
                "br" -> {
                    builder.append("  \n")
                }
                
                // Horizontal rule
                "hr" -> {
                    builder.append("\n\n---\n\n")
                }
                
                // Links
                "a" -> {
                    val href = node.attr("href")
                    val text = buildString {
                        node.childNodes().forEach { convertToMarkdown(it, this) }
                    }.trim()
                    
                    if (text.isNotBlank()) {
                        if (href.isNotBlank()) {
                            builder.append("[$text]($href)")
                        } else {
                            builder.append(text)
                        }
                    }
                }
                
                // Images
                "img" -> {
                    val src = node.attr("src")
                    val alt = node.attr("alt")
                    if (src.isNotBlank()) {
                        builder.append("![${alt}]($src)")
                    }
                }
                
                // Emphasis
                "strong", "b" -> {
                    builder.append("**")
                    node.childNodes().forEach { convertToMarkdown(it, builder) }
                    builder.append("**")
                }
                
                "em", "i" -> {
                    builder.append("*")
                    node.childNodes().forEach { convertToMarkdown(it, builder) }
                    builder.append("*")
                }
                
                "code" -> {
                    // Check if it's a code block
                    val parent = node.parent()
                    val isCodeBlock = parent?.tagName()?.lowercase() == "pre"
                    
                    if (isCodeBlock) {
                        // Will be handled by "pre" element
                    } else {
                        // Inline code
                        builder.append("`")
                        node.childNodes().forEach { convertToMarkdown(it, builder) }
                        builder.append("`")
                    }
                }
                
                "pre" -> {
                    val code = node.selectFirst("code")
                    val text = code?.wholeText() ?: node.wholeText()
                    if (text.isNotBlank()) {
                        builder.append("\n\n```\n")
                        builder.append(text.trim())
                        builder.append("\n```\n\n")
                    }
                }
                
                // Blockquotes
                "blockquote" -> {
                    builder.append("\n\n")
                    val text = buildString {
                        node.childNodes().forEach { convertToMarkdown(it, this) }
                    }.trim()
                    
                    if (text.isNotBlank()) {
                        text.split("\n").forEach { line ->
                            builder.append("> ")
                            builder.append(line.trim())
                            builder.append("\n")
                        }
                    }
                    builder.append("\n")
                }
                
                // Unordered lists
                "ul" -> {
                    builder.append("\n\n")
                    node.children().forEach { item ->
                        if (item.tagName().lowercase() == "li") {
                            builder.append("- ")
                            item.childNodes().forEach { convertToMarkdown(it, builder) }
                            builder.append("\n")
                        }
                    }
                    builder.append("\n")
                }
                
                // Ordered lists
                "ol" -> {
                    builder.append("\n\n")
                    var counter = 1
                    node.children().forEach { item ->
                        if (item.tagName().lowercase() == "li") {
                            builder.append("$counter. ")
                            item.childNodes().forEach { convertToMarkdown(it, builder) }
                            builder.append("\n")
                            counter++
                        }
                    }
                    builder.append("\n")
                }
                
                // Tables
                "table" -> {
                    builder.append("\n\n")
                    node.childNodes().forEach { convertToMarkdown(it, builder) }
                    builder.append("\n\n")
                }
                
                "thead", "tbody" -> {
                    node.childNodes().forEach { convertToMarkdown(it, builder) }
                }
                
                "tr" -> {
                    node.children().forEach { cell ->
                        val text = buildString {
                            cell.childNodes().forEach { convertToMarkdown(it, this) }
                        }.trim()
                        builder.append("| $text ")
                    }
                    builder.append("|\n")
                    
                    // Add separator after header row
                    if (node.parent()?.tagName()?.lowercase() == "thead") {
                        node.children().forEach {
                            builder.append("| --- ")
                        }
                        builder.append("|\n")
                    }
                }
                
                // Container elements
                "div", "article", "section", "main", "figure", "figcaption" -> {
                    builder.append("\n\n")
                    node.childNodes().forEach { convertToMarkdown(it, builder) }
                    builder.append("\n\n")
                }
                
                else -> {
                    // For other elements, just process children
                    node.childNodes().forEach { convertToMarkdown(it, builder) }
                }
            }
        }
        else -> {
            // For other node types, process children
            node.childNodes().forEach { convertToMarkdown(it, builder) }
        }
    }
}

/**
 * Normalizes whitespace in a string:
 * - Replaces multiple spaces with single space
 * - Replaces multiple newlines with double newline (paragraph separator)
 * - Removes leading/trailing whitespace from each line
 * - Collapses empty lines
 */
private fun String.normalizeWhitespace(): String {
    return this
        // Split by newlines
        .split("\n")
        // Process each line
        .map { line ->
            // Replace multiple spaces with single space and trim
            line.replace(Regex("\\s+"), " ").trim()
        }
        // Join back with newlines
        .joinToString("\n")
        // Replace 3+ consecutive newlines with 2 newlines
        .replace(Regex("\n{3,}"), "\n\n")
}

/**
 * Validates and sanitizes text output:
 * - Ensures no excessive blank lines
 * - Removes trailing whitespace
 * - Returns empty string if result is only whitespace
 */
private fun String.validateOutput(): String {
    val trimmed = trim()
    
    // If it's only whitespace, return empty string
    if (trimmed.isEmpty()) {
        return ""
    }
    
    // Remove excessive blank lines (more than 2 consecutive newlines)
    return trimmed.replace(Regex("\n{3,}"), "\n\n")
}
