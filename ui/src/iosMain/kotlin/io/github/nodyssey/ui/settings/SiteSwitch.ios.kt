package io.github.nodyssey.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.nodyssey.core.ActiveSite
import io.github.nodyssey.core.Site
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberSiteSwitch(): (Site) -> Unit =
    remember {
        { site ->
            if (site != ActiveSite.current) {
                IosActiveSite.store(site)
                IosActiveSite.switcher?.invoke(site)
            }
        }
    }

/**
 * iOS switches in place: the choice is stored, and the shell tears down its screens and its graph and
 * builds both again over the other site — see `NodysseyApp.switchSite` in `:iosapp`.
 *
 * It cannot restart the process the way Android does — terminating an app from inside it reads to
 * the user as a crash — so the rebuild is what makes a switch one tap. What used to rule that out,
 * two graphs over the same DataStore files, is answered by the files being opened once per process
 * and handed to each graph in turn (`ProcessWideInstances`), and by the old graph being stopped
 * before the site changes under it.
 */
actual val siteSwitchRestartsApp: Boolean = true

/** 站点, as an Apple platform stores it. The Android note on [AndroidActiveSite] applies here too. */
object IosActiveSite {
    private const val KEY_SITE = "active_site"

    /** Reads the stored choice and installs it. Called from the shell's entry point, once. */
    fun install() {
        ActiveSite.install(Site.ofStoredValue(NSUserDefaults.standardUserDefaults.stringForKey(KEY_SITE)))
    }

    internal fun store(site: Site) {
        NSUserDefaults.standardUserDefaults.setObject(site.storedValue, KEY_SITE)
    }

    /**
     * What rebuilds the app on [site]; set by the shell once it is up. Null before that, in which case
     * the stored choice simply applies at the next launch.
     */
    var switcher: ((Site) -> Unit)? = null
}
