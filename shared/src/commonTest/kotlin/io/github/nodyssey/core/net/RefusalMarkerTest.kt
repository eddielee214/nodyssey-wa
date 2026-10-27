package io.github.nodyssey.core.net

import io.github.nodyssey.core.NodeSeekSite
import io.github.nodyssey.core.html.Fixtures
import io.github.plaza.core.net.ChallengeDetector
import io.github.plaza.core.net.SiteError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The refusal wording counts only when the site says it, not when a member does.
 *
 * What went wrong for readers on 2026-10-27: a front-page thread quoting 权限不足 made the front page
 * itself read as a sign-in wall, and every signed-in screen built on it — 我的 first — showed
 * 登录状态没被认出来 to everyone at once.
 */
class RefusalMarkerTest {
    private val detector = ChallengeDetector(NodeSeekSite.CONFIG.markers)

    private fun detect(html: String): SiteError? = detector.detect(html, 200, emptyMap(), "https://www.nodeseek.com/")

    @Test
    fun `a thread title quoting a refusal does not make the feed a sign-in wall`() {
        val feed =
            """
            <html><body><div id="nsk-body"><ul class="post-list">
              <li class="post-list-item"><div class="post-title"><a>打开帖子提示权限不足是怎么回事</a></div></li>
              <li class="post-list-item"><div class="post-title"><a>Wrong category 又出现了</a></div></li>
            </ul></div></body></html>
            """.trimIndent()
        assertNull(detect(feed))
    }

    @Test
    fun `a script or a signature carrying the wording is not a refusal either`() {
        val page =
            """
            <html><head><script>const tip = "需要注册用户才能查看";</script></head>
            <body><div id="nsk-body">
              <article class="post-content">我也遇到了：本帖需要注册用户才能查看</article>
              <div class="signature">签名：权限不足</div>
            </div></body></html>
            """.trimIndent()
        assertNull(detect(page))
    }

    @Test
    fun `the site's own refusals are still recognised`() {
        assertEquals(SiteError.LoginRequired, detect(Fixtures.load("post-login-required.html")))
        assertEquals(SiteError.LevelRequired(5), detect(Fixtures.load("post-level-required.html")))
        assertEquals(SiteError.PrivatePost, detect(Fixtures.load("post-private.html")))
    }
}
