package eu.rekisoft.android.util

import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting

/**
 * Helper class to create Handler and check if the code is executed on the main thread.
 * Can be easily mocked for testing.
 */
object ThreadingHelper {
    /** Inject your own Handler just for testing. Set to `null` for default behavior. */
    @VisibleForTesting
    var mockHandler: Handler? = null

    /** Enforce the `isOnMainThread` result just for testing. Set to `null` for default behavior. */
    @VisibleForTesting
    var mockIsOnMainThread: Boolean? = null

    /** Create a new Handler */
    fun createHandler(): Handler = mockHandler ?: Handler(Looper.getMainLooper())

    /** Returns `true` when this property is read on the main thread. */
    @get:JvmName("isOnMainThread")
    val isOnMainThread: Boolean
        get() = mockIsOnMainThread ?: Looper.getMainLooper().thread == Thread.currentThread()
}