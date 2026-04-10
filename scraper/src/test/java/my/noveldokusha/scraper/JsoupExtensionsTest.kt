package my.noveldokusha.scraper

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsoupExtensionsTest {

    @Test
    fun `toText extracts plain text from HTML`() {
        val html = """
            <div>
                <h1>Title</h1>
                <p>This is a paragraph.</p>
                <p>This is another paragraph with <strong>bold text</strong>.</p>
            </div>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val text = doc.body().toText()

        assertTrue(text.contains("Title"))
        assertTrue(text.contains("This is a paragraph."))
        assertTrue(text.contains("This is another paragraph with bold text."))
    }

    @Test
    fun `toText handles lists correctly`() {
        val html = """
            <ul>
                <li>Item 1</li>
                <li>Item 2</li>
                <li>Item 3</li>
            </ul>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val text = doc.body().toText()

        assertTrue(text.contains("Item 1"))
        assertTrue(text.contains("Item 2"))
        assertTrue(text.contains("Item 3"))
    }

    @Test
    fun `toText normalizes whitespace`() {
        val html = """
            <p>   Multiple    spaces   here   </p>
            <p>
                Multiple
                newlines
            </p>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val text = doc.body().toText()

        // Should normalize multiple spaces to single space
        assertTrue(text.contains("Multiple spaces here"))
        assertTrue(text.contains("Multiple"))
        assertTrue(text.contains("newlines"))
    }

    @Test
    fun `toText removes script and style tags`() {
        val html = """
            <div>
                <p>Visible text</p>
                <script>alert('hidden');</script>
                <style>.hidden { display: none; }</style>
            </div>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val text = doc.body().toText()

        assertTrue(text.contains("Visible text"))
        assertTrue(!text.contains("alert"))
        assertTrue(!text.contains("hidden"))
    }

    @Test
    fun `toMarkdown converts headings`() {
        val html = """
            <h1>Heading 1</h1>
            <h2>Heading 2</h2>
            <h3>Heading 3</h3>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val markdown = doc.body().toMarkdown()

        assertTrue(markdown.contains("# Heading 1"))
        assertTrue(markdown.contains("## Heading 2"))
        assertTrue(markdown.contains("### Heading 3"))
    }

    @Test
    fun `toMarkdown converts links`() {
        val html = """
            <a href="https://example.com">Click here</a>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val markdown = doc.body().toMarkdown()

        assertTrue(markdown.contains("[Click here](https://example.com)"))
    }

    @Test
    fun `toMarkdown converts images`() {
        val html = """
            <img src="image.jpg" alt="An image">
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val markdown = doc.body().toMarkdown()

        assertTrue(markdown.contains("![An image](image.jpg)"))
    }

    @Test
    fun `toMarkdown converts emphasis`() {
        val html = """
            <p><strong>Bold text</strong> and <em>italic text</em></p>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val markdown = doc.body().toMarkdown()

        assertTrue(markdown.contains("**Bold text**"))
        assertTrue(markdown.contains("*italic text*"))
    }

    @Test
    fun `toMarkdown converts unordered lists`() {
        val html = """
            <ul>
                <li>Item 1</li>
                <li>Item 2</li>
                <li>Item 3</li>
            </ul>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val markdown = doc.body().toMarkdown()

        assertTrue(markdown.contains("- Item 1"))
        assertTrue(markdown.contains("- Item 2"))
        assertTrue(markdown.contains("- Item 3"))
    }

    @Test
    fun `toMarkdown converts ordered lists`() {
        val html = """
            <ol>
                <li>First</li>
                <li>Second</li>
                <li>Third</li>
            </ol>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val markdown = doc.body().toMarkdown()

        assertTrue(markdown.contains("1. First"))
        assertTrue(markdown.contains("2. Second"))
        assertTrue(markdown.contains("3. Third"))
    }

    @Test
    fun `toMarkdown converts blockquotes`() {
        val html = """
            <blockquote>
                <p>This is a quote</p>
            </blockquote>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val markdown = doc.body().toMarkdown()

        assertTrue(markdown.contains("> This is a quote"))
    }

    @Test
    fun `toMarkdown converts code blocks`() {
        val html = """
            <pre><code>function hello() {
    console.log("Hello");
}</code></pre>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val markdown = doc.body().toMarkdown()

        assertTrue(markdown.contains("```"))
        assertTrue(markdown.contains("function hello()"))
    }

    @Test
    fun `toMarkdown converts inline code`() {
        val html = """
            <p>Use the <code>println()</code> function</p>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val markdown = doc.body().toMarkdown()

        assertTrue(markdown.contains("`println()`"))
    }

    @Test
    fun `toMarkdown converts horizontal rules`() {
        val html = """
            <hr>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val markdown = doc.body().toMarkdown()

        assertTrue(markdown.contains("---"))
    }

    @Test
    fun `toMarkdown handles complex HTML`() {
        val html = """
            <article>
                <h1>Article Title</h1>
                <p>This is an article with <strong>bold</strong> and <em>italic</em> text.</p>
                
                <h2>Section 1</h2>
                <p>Some text with a <a href="https://example.com">link</a>.</p>
                
                <ul>
                    <li>List item 1</li>
                    <li>List item 2</li>
                </ul>
                
                <blockquote>
                    <p>An important quote</p>
                </blockquote>
            </article>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val markdown = doc.body().toMarkdown()

        assertTrue(markdown.contains("# Article Title"))
        assertTrue(markdown.contains("**bold**"))
        assertTrue(markdown.contains("*italic*"))
        assertTrue(markdown.contains("[link](https://example.com)"))
        assertTrue(markdown.contains("- List item 1"))
        assertTrue(markdown.contains("> An important quote"))
    }

    @Test
    fun `toText handles empty elements`() {
        val html = """
            <div>
                <p></p>
                <p>   </p>
                <p>Real content</p>
            </div>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val text = doc.body().toText()

        assertTrue(text.contains("Real content"))
    }

    @Test
    fun `both methods handle nested elements correctly`() {
        val html = """
            <div>
                <p>
                    <span>
                        <span>
                            Deep nested text
                        </span>
                    </span>
                </p>
            </div>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val text = doc.body().toText()
        val markdown = doc.body().toMarkdown()

        assertTrue(text.contains("Deep nested text"))
        assertTrue(markdown.contains("Deep nested text"))
    }
}
