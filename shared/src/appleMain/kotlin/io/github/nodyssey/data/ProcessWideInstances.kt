package io.github.nodyssey.data

import platform.Foundation.NSLock

/**
 * One instance per key for the life of the process, whichever thread asks first.
 *
 * For the handles an Apple process must never hold two of over one file — a DataStore, a Room
 * database. The iOS shell can build its dependency graph more than once in a process (a 站点 switch
 * rebuilds it in place), and each build asks for its files again; this is what turns the second ask
 * into the first answer instead of a second handle.
 */
internal class ProcessWideInstances<T : Any> {
    private val lock = NSLock()
    private val instances = mutableMapOf<String, T>()

    fun getOrOpen(
        key: String,
        open: () -> T,
    ): T {
        lock.lock()
        try {
            return instances.getOrPut(key, open)
        } finally {
            lock.unlock()
        }
    }
}
