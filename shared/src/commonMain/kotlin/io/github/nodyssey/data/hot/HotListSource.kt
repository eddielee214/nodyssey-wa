package io.github.nodyssey.data.hot

import io.github.plaza.core.AppDispatchers
import io.github.plaza.core.net.HttpRequest
import io.github.plaza.core.net.HttpTransport
import io.github.plaza.core.net.SiteException
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/** Which of the three lists: the last hour or so, the day, the week. */
enum class HotListPeriod(internal val file: String) {
    Realtime("hot"),
    Daily("daily"),
    Weekly("weekly"),
}

/** One thread on a 热榜, in rank order. */
data class HotListEntry(
    val postId: Long,
    val title: String,
    val authorName: String,
    val authorUid: Long?,
    val views: Int,
    val comments: Int,
    /** The list's own ranking number; only its order means anything. */
    val score: Double,
)

data class HotList(
    val entries: List<HotListEntry>,
    /** When the service last rebuilt this list, epoch seconds; null when it did not say. */
    val updatedAtEpochSeconds: Long?,
)

enum class HotListError { Network, Unavailable, Unparsable }

class HotListException(val error: HotListError) : Exception(error.name)

/**
 * NodeSeek 热榜 — the ranking the "NodeSeek 热榜插件" userscript shows in the site's sidebar.
 *
 * NodeSeek itself publishes no such list. This one is computed and hosted by a third party,
 * `api.bimg.eu.org`, from public thread data: three static JSON files, one per [HotListPeriod], each
 * `{ "updated_at": <seconds>, "posts": [{ "post": {…}, "score": … }] }` already in rank order.
 *
 * Which is why [http] must be a transport with no NodeSeek cookie on it — the same one the image hosts
 * and the sticker packs use. Nothing about the reader's account goes to this service; it is asked for
 * a public file and nothing else.
 */
class HotListSource(
    private val http: HttpTransport,
    /** [HttpTransport.execute] blocks on Android, and the screen calls in from the main thread. */
    private val dispatchers: AppDispatchers,
) {
    suspend fun hotList(period: HotListPeriod): HotList {
        val response =
            try {
                withContext(dispatchers.io) {
                    http.execute(HttpRequest(url = "$BASE/${period.file}.json", headers = mapOf("Accept" to "application/json")))
                }
            } catch (_: SiteException) {
                throw HotListException(HotListError.Network)
            }
        if (!response.isSuccessful) throw HotListException(HotListError.Unavailable)
        return parseHotList(response.body)
    }

    private companion object {
        const val BASE = "https://api.bimg.eu.org"
    }
}

private val HotListJson = Json { ignoreUnknownKeys = true }

/** One of the three files, read leniently: a row missing its id is dropped rather than failing the list. */
internal fun parseHotList(body: String): HotList {
    val root =
        runCatching { HotListJson.parseToJsonElement(body).jsonObject }.getOrNull()
            ?: throw HotListException(HotListError.Unparsable)
    val posts =
        runCatching { root["posts"]!!.jsonArray }.getOrNull()
            ?: throw HotListException(HotListError.Unparsable)
    val entries =
        posts.mapNotNull { element ->
            val item = element as? JsonObject ?: return@mapNotNull null
            val post = item["post"] as? JsonObject ?: return@mapNotNull null
            val id = post.long("id") ?: return@mapNotNull null
            HotListEntry(
                postId = id,
                title = post.string("title").orEmpty(),
                authorName = post.string("author").orEmpty(),
                authorUid = post.long("author_id"),
                views = post.int("views") ?: 0,
                comments = post.int("comments") ?: 0,
                score = item["score"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
            )
        }
    return HotList(entries = entries, updatedAtEpochSeconds = root.long("updated_at"))
}

private fun JsonObject.string(key: String): String? = runCatching { get(key)?.jsonPrimitive?.contentOrNull }.getOrNull()

private fun JsonObject.long(key: String): Long? = runCatching { get(key)?.jsonPrimitive?.longOrNull }.getOrNull()

private fun JsonObject.int(key: String): Int? = runCatching { get(key)?.jsonPrimitive?.intOrNull }.getOrNull()
