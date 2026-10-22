package io.github.nodyssey.core.html

import com.fleeksoft.ksoup.Ksoup
import io.github.plaza.core.richtext.InlineNode
import io.github.plaza.core.richtext.RichNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `> [!note] 标题` callouts, in the markup the site's editor plugin writes for them.
 *
 * Shape taken from that plugin's renderer (post-999857 opens with one): the title is a span holding
 * an icon `<svg>` and the title text, and the body follows as ordinary blocks.
 */
class CalloutParserTest {

    private fun parse(html: String) = RichContentParser.parse(Ksoup.parse("<article>$html</article>").selectFirst("article"))

    private val icon =
        """<svg class="markdown-alert-icon" viewBox="0 0 16 16" version="1.1" width="16" height="16" aria-hidden="true">
          <path d="M0 8a8 8 0 1 1 16 0A8 8 0 0 1 0 8Z">
          </path>
        </svg>"""

    @Test
    fun `a callout keeps its title apart from its body`() {
        val nodes =
            parse(
                """
                <div class="markdown-alert markdown-alert-note" dir="auto" >
                      <span class="markdown-alert-title">
                        $icon
                        价格&amp;交付
                      </span>
                <p>价格：5刀/月<br>
                交付sk5</p>
                </div>
                <h2>地区和质量</h2>
                """.trimIndent(),
            )

        val callout = nodes.first() as RichNode.Callout
        assertEquals("note", callout.kind)
        assertEquals("价格&交付", callout.title)
        assertFalse(callout.collapsible)
        val body = callout.children.single() as RichNode.Paragraph
        assertEquals("价格：5刀/月", (body.inlines.first() as InlineNode.Text).text)
        assertTrue(nodes[1] is RichNode.Heading)
    }

    @Test
    fun `the foldable form is a collapsible callout rather than a plain fold`() {
        val nodes =
            parse(
                """
                <details class="markdown-alert markdown-alert-warning" dir="auto" open>
                      <summary class="markdown-alert-title">$icon Warning </summary>
                <p>注意</p>
                </details>
                """.trimIndent(),
            )

        val callout = nodes.single() as RichNode.Callout
        assertEquals("warning", callout.kind)
        assertEquals("Warning", callout.title)
        assertTrue(callout.collapsible)
        assertTrue(callout.open)
        assertEquals(1, callout.children.size)
    }

    @Test
    fun `an ordinary details block is still a fold`() {
        val nodes = parse("<details><summary>折叠</summary><p>内容</p></details>")

        assertTrue(nodes.single() is RichNode.Fold)
    }
}
