package io.github.plaza.core.net

import com.fleeksoft.ksoup.Ksoup

/**
 * A scraped forum sits behind Cloudflare, and some of its boards require a signed-in account. Both
 * failures come back as a 200-with-HTML rather than a useful status code, so the body has to be
 * inspected.
 *
 * Pure logic on purpose: it is covered by JVM unit tests against captured fixtures. What counts as
 * which failure is [markers], because only the site knows its own wording.
 */
class ChallengeDetector(private val markers: PageMarkers) {

    /**
     * Returns the reason this response is unusable, or `null` when it carries real content.
     *
     * [url] is the address that was requested, and it is here for one case: a challenge is cleared
     * by opening the refused address in a web view, and only the caller knows what that was. See
     * [SiteError.Cloudflare].
     */
    fun detect(html: String, statusCode: Int, headers: Map<String, String>, url: String): SiteError? {
        // The refusals are read from what the site itself says on the page, not from the raw body.
        // A member's thread title, a quoted error, a signature or a script string can all carry
        // 权限不足; matched against the raw body, one such title on the front page made every
        // signed-in screen that loads it report an unrecognised session (2026-10-27). Parsed only when
        // a phrase is there at all, so an ordinary page costs nothing extra; the Cloudflare markers
        // below stay on the raw body, since those are scripts.
        val refusalCandidate =
            markers.levelRequired.any { it.containsMatchIn(html) } ||
                markers.privatePost.any { html.contains(it) } ||
                markers.loginRequired.any { html.contains(it) }
        val siteText = if (refusalCandidate) siteText(html) else ""

        // Before the login markers, not after: see [PageMarkers.levelRequired] — the two refusals
        // can share a phrase, and only the more specific one carries a level to show.
        markers.levelRequired.firstNotNullOfOrNull { it.find(siteText) }?.let { match ->
            return SiteError.LevelRequired(match.groupValues.getOrNull(1)?.toIntOrNull())
        }

        // Beside the level wall and for the same reason: the page it arrives on is an ordinary
        // signed-in frame carrying `id="nsk-body"`, so anything checked after [markers.usablePage]
        // would never see it. Its 404 is invisible for the same reason.
        if (markers.privatePost.any { siteText.contains(it) }) return SiteError.PrivatePost

        if (markers.loginRequired.any { siteText.contains(it) }) return SiteError.LoginRequired

        // A normal page from a Cloudflare-fronted site also carries `Server: cloudflare`, so only an
        // explicit challenge signal counts — and real content always wins over a false positive.
        if (markers.usablePage.any { html.contains(it) }) return null

        val normalized = headers.mapKeys { it.key.lowercase() }
        if (normalized["cf-mitigated"]?.lowercase() == "challenge") return SiteError.Cloudflare(url)
        if (markers.challenge.any { html.contains(it) }) return SiteError.Cloudflare(url)

        // After the Cloudflare checks, before the generic status handling: a 429 that carried a
        // challenge is Cloudflare's, everything else is the site's own throttle, and "HTTP 429" as a
        // screen title tells the reader nothing they can act on.
        if (statusCode == 429 || markers.rateLimit.any { html.contains(it) }) {
            return SiteError.RateLimited
        }

        if (statusCode !in 200..299) return SiteError.Http(statusCode)

        return SiteError.Unparsable
    }

    /** The page's visible text with members' writing, scripts and form fields taken out. */
    private fun siteText(html: String): String {
        val document = Ksoup.parse(html)
        document.select("head, script, style, template, textarea").remove()
        markers.userContentSelectors.forEach { document.select(it).remove() }
        return document.body().text()
    }
}
