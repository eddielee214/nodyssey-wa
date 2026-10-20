package io.github.nodyssey.data.hot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class HotListParserTest {
    @Test
    fun `reads the rows in rank order with their counts`() {
        val list =
            parseHotList(
                """
                {"updated_at": 1792503000, "posts": [
                  {"post": {"id": 996823, "title": "为了aff 脸都不要了吗", "author": "锦鲤", "author_id": 21741,
                            "post_timestamp": 1792502986, "views": 911, "comments": 22, "summary": "", "category": "daily"},
                   "score": 245.56},
                  {"post": {"id": 12, "title": "second", "author": "b", "views": 3, "comments": 1}, "score": 9.5}
                ]}
                """.trimIndent(),
            )

        assertEquals(1792503000L, list.updatedAtEpochSeconds)
        assertEquals(listOf(996823L, 12L), list.entries.map { it.postId })
        val first = list.entries.first()
        assertEquals("为了aff 脸都不要了吗", first.title)
        assertEquals("锦鲤", first.authorName)
        assertEquals(21741L, first.authorUid)
        assertEquals(911, first.views)
        assertEquals(22, first.comments)
        assertNull(list.entries[1].authorUid)
    }

    @Test
    fun `drops a row with no id instead of failing the list`() {
        val list = parseHotList("""{"posts": [{"post": {"title": "no id"}}, {"post": {"id": 7, "title": "ok"}}]}""")

        assertEquals(listOf(7L), list.entries.map { it.postId })
        assertNull(list.updatedAtEpochSeconds)
    }

    @Test
    fun `a body that is not the list is unparsable`() {
        val failure = assertFailsWith<HotListException> { parseHotList("<html>502</html>") }
        assertEquals(HotListError.Unparsable, failure.error)
    }
}
