package io.github.nodyssey

import androidx.navigation3.runtime.NavKey
import io.github.nodyssey.ui.login.WebViewGoal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What a two-pane window is showing, read off the back stack.
 *
 * The cases that matter are the ones where the answer is not the tab: a thread reached from a screen
 * that is not a list is still full-screen, and a list reached from a thread still counts.
 *
 * These also stand in for the scene metadata, which no test can read back: the entries carry none of
 * their own and `paneMetadataOf` derives all of it from [paneRoleOf].
 */
class PaneRoleTest {
    @Test
    fun `a thread under the feed shares the window with it`() {
        assertTrue(listOf<NavKey>(PostListKey, PostDetailKey(1)).showsListPane())
    }

    @Test
    fun `a thread opened from a user's space shares the window with it`() {
        val stack =
            listOf<NavKey>(ProfileKey, UserSpaceKey(uid = 7), PostDetailKey(1))
        assertTrue(stack.showsListPane())
    }

    @Test
    fun `a user's space opened from a thread is itself the list`() {
        val stack =
            listOf<NavKey>(PostListKey, PostDetailKey(1), UserSpaceKey(uid = 7))
        assertTrue(stack.showsListPane())
    }

    /**
     * 追踪的帖子 carries no pane metadata, so the scene strategy stops there and hands the whole window
     * to the thread. Answering otherwise would take away the only way back out of it.
     */
    @Test
    fun `a thread opened from tracked threads is full-screen`() {
        val stack = listOf<NavKey>(ProfileKey, TrackedThreadsKey, PostDetailKey(1))
        assertFalse(stack.showsListPane())
    }

    @Test
    fun `a full-screen destination on top of a pair ends the run`() {
        val stack =
            listOf<NavKey>(
                PostListKey,
                PostDetailKey(1),
                WebKey("https://example.test", "NodeSeek", WebViewGoal.MANAGE),
            )
        assertFalse(stack.showsListPane())
    }

    @Test
    fun `the thread beside the feed has nothing to go back to`() {
        val thread = PostDetailKey(1)
        assertTrue(listOf(PostListKey, thread).sitsBesideList(thread))
    }

    /** It covers the first thread in the same half of the window, so it keeps its arrow. */
    @Test
    fun `a thread opened from that thread can go back to it`() {
        val first = PostDetailKey(1)
        val second = PostDetailKey(2)
        val stack = listOf(PostListKey, first, second)
        assertTrue(stack.sitsBesideList(first))
        assertFalse(stack.sitsBesideList(second))
    }

    @Test
    fun `a thread from tracked threads is not beside a list`() {
        val thread = PostDetailKey(1)
        assertFalse(listOf(ProfileKey, TrackedThreadsKey, thread).sitsBesideList(thread))
    }

    @Test
    fun `a row tapped beside an open thread replaces it`() {
        val stack = mutableListOf<NavKey>(PostListKey, PostDetailKey(1), PostDetailKey(2))
        stack.openFromList(PostListKey, PostDetailKey(3), expanded = true)
        assertEquals(listOf<NavKey>(PostListKey, PostDetailKey(3)), stack)
    }

    @Test
    fun `the row already open is left alone`() {
        val open = PostDetailKey(1)
        val stack = mutableListOf<NavKey>(PostListKey, open)
        stack.openFromList(PostListKey, PostDetailKey(1), expanded = true)
        assertTrue(stack.last() === open)
    }

    @Test
    fun `on a narrow window a row is an ordinary push`() {
        val stack = mutableListOf<NavKey>(PostListKey, PostDetailKey(1))
        stack.openFromList(PostListKey, PostDetailKey(2), expanded = false)
        assertEquals(listOf<NavKey>(PostListKey, PostDetailKey(1), PostDetailKey(2)), stack)
    }

    @Test
    fun `a thread opened from the hot list sits beside it`() {
        val thread = PostDetailKey(1)
        val stack = listOf(PostListKey, HotListKey, thread)
        assertTrue(stack.showsListPane())
        assertTrue(stack.sitsBesideList(thread))
    }

    /** The arrow on the hot list used to close the thread beside it instead of the list. */
    @Test
    fun `a list's own back closes the list and what is open beside it`() {
        val stack = mutableListOf<NavKey>(PostListKey, HotListKey, PostDetailKey(1), PostDetailKey(2))
        stack.closeList(HotListKey)
        assertEquals(listOf<NavKey>(PostListKey), stack)
    }

    @Test
    fun `on a narrow window a list's back is the one pop it always was`() {
        val stack = mutableListOf<NavKey>(PostListKey, HotListKey)
        stack.closeList(HotListKey)
        assertEquals(listOf<NavKey>(PostListKey), stack)
    }

    @Test
    fun `my own lists of threads sit beside what they open`() {
        listOf(MyTopicsKey, MyCommentsKey, BookmarksKey, ReadHistoryKey).forEach { list ->
            val thread = PostDetailKey(1)
            assertTrue(listOf(ProfileKey, list, thread).sitsBesideList(thread))
        }
    }
}
