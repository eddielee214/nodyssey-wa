package io.github.nodyssey

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UINavigationController
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController
import platform.UIKit.removeFromParentViewController
import platform.UIKit.separatorColor
import platform.UIKit.tabBarController
import platform.UIKit.willMoveToParentViewController

/**
 * A list and its details side by side: the iPad's answer to `ListDetailSceneStrategy`.
 *
 * The list is one page, held as a child; the details are a [UINavigationController] of their own, so
 * opening one thread from another pushes it in the right half and a swipe there pops it, the way
 * Mail and Notes behave. The shell owns both — this only lays them out.
 *
 * A fixed-width list rather than a share of the window: it is a column of titles, and past the
 * width of a phone it gains nothing but white space, which the thread beside it could use instead.
 */
@OptIn(ExperimentalForeignApi::class)
internal class PaneSplitController(
    val details: UINavigationController,
    /** How much of its top inset the list is to treat as not there; see [returnTabBarHeightToList]. */
    private val onListTopReturned: (Double) -> Unit,
) : UIViewController(nibName = null, bundle = null) {
    private var returnedTop = 0.0
    private val divider = UIView()
    private var list: UIViewController? = null

    override fun viewDidLoad() {
        super.viewDidLoad()
        adopt(details)
        divider.backgroundColor = UIColor.separatorColor
        view.addSubview(divider)
    }

    /** Puts [controller] in the left half, or empties it so the page can be shown somewhere else. */
    fun showList(controller: UIViewController?) {
        if (list == controller) return
        list?.let {
            // What [viewDidLayoutSubviews] gave the list back belongs to this column only.
            reportReturnedTop(0.0)
            it.willMoveToParentViewController(null)
            it.view.removeFromSuperview()
            it.removeFromParentViewController()
        }
        list = controller
        if (controller != null) adopt(controller)
        view.setNeedsLayout()
    }

    private fun adopt(child: UIViewController) {
        addChildViewController(child)
        view.addSubview(child.view)
        child.didMoveToParentViewController(this)
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        val scale = view.traitCollection.displayScale.takeIf { it > 0.0 } ?: 2.0
        val hairline = 1.0 / scale
        view.bounds.useContents {
            val height = size.height
            list?.view?.setFrame(CGRectMake(0.0, 0.0, LIST_PANE_WIDTH, height))
            divider.setFrame(CGRectMake(LIST_PANE_WIDTH, 0.0, hairline, height))
            details.view.setFrame(CGRectMake(LIST_PANE_WIDTH + hairline, 0.0, size.width - LIST_PANE_WIDTH - hairline, height))
        }
        returnTabBarHeightToList()
    }

    /**
     * iPad's tab bar floats at the top of the window, centred, and UIKit gives every child its height
     * as safe area — the list as well, though on a window this wide the bar is nowhere above it, and
     * the list's header sat under a band of nothing. The list gets that height back, as long as the
     * bar really does clear its column; in a window narrow enough for the bar to reach over the list,
     * or if the bar cannot be found, the inset stays.
     *
     * Handed to the page rather than set as `additionalSafeAreaInsets`: UIKit reads a negative one as
     * zero, so the page consumes that much of its own top inset instead.
     */
    private fun returnTabBarHeightToList() {
        if (list == null) return
        val ownTop = view.safeAreaInsets.useContents { top }
        val windowTop = view.window?.safeAreaInsets?.useContents { top } ?: ownTop
        val barStart = if (ownTop > windowTop) floatingTabBarStart(ownTop) else null
        reportReturnedTop(if (barStart != null && barStart >= LIST_PANE_WIDTH) ownTop - windowTop else 0.0)
    }

    private fun reportReturnedTop(top: Double) {
        if (top == returnedTop) return
        returnedTop = top
        onListTopReturned(top)
    }

    /**
     * Where the floating tab bar starts, in this controller's coordinates, or null.
     *
     * `tabBarController.tabBar` is not it: that is the classic bar, parked off the bottom of the
     * window. The floating one has no public handle, so it is found by shape — something in the band
     * the top inset covers, not as wide as the window — among the tab controller's own views, never
     * descending into the one that holds the pages.
     */
    private fun floatingTabBarStart(bandBottom: Double): Double? {
        val tabs = tabBarController?.view ?: return null
        val width = view.bounds.useContents { size.width }
        var start: Double? = null
        fun visit(
            candidate: UIView,
            depth: Int,
        ) {
            if (view.isDescendantOfView(candidate)) {
                // An ancestor of this split: the pages are in there, and so is nothing else.
                if (candidate != tabs) return
            } else if (!candidate.hidden) {
                candidate.convertRect(candidate.bounds, toView = view).useContents {
                    val isBar =
                        origin.y >= 0.0 && origin.y + size.height <= bandBottom + 1.0 &&
                            size.height >= 30.0 && size.width >= 100.0 && size.width < width * 0.8
                    if (isBar) start = minOf(start ?: origin.x, origin.x)
                }
            }
            if (depth < 4) candidate.subviews.forEach { visit(it as UIView, depth + 1) }
        }
        visit(tabs, 0)
        return start
    }
}

/** Material's default list pane width, which Android's two-pane layout uses too. */
private const val LIST_PANE_WIDTH = 360.0
