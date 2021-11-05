/**
 * @copyright
 * This code is licensed under the Rekisoft Public License.
 * See http://www.rekisoft.eu/licenses/rkspl.html for more informations.
 */
/**
 * @package eu.rekisoft.android.util
 * This package contains utilities provided by [rekisoft.eu](https://rekisoft.eu/).
 */
package eu.rekisoft.android.util;

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias Lambda = () -> Unit
typealias SuspendFunction = suspend () -> Unit

/**
 * Utility for executing delayed tasks, which can been canceled. It you add a task multiple times it
 * will been executed after the least desired delay.
 *
 * @author René Kilczan
 * @version 2.1.0
 * @copyright This code is licensed under the Rekisoft Public License.<br/>
 * See https://www.rekisoft.eu/licenses/rkspl.html for more information.
 */
object LazyWorker {
    /**
     * Create a life cycle aware new Job
     * @param lifecycle the lifecycle of the owner
     * @param work the lambda to executed later
     */
    @JvmStatic
    fun createLifeCycleAwareJob(lifecycle: Lifecycle, work: Job<Lambda>.() -> Unit): Job<Lambda> = MainThreadJob(lifecycle, work)

    /**
     * Create a life cycle aware new Job
     * @param lifecycle the lifecycle of the owner
     * @param work the lambda to executed later
     */
    @JvmStatic
    fun createLifeCycleAwareJob(context: Context, work: Job<Lambda>.() -> Unit): Job<Lambda> = MainThreadJob(context.findLifecycle(), work)

    /**
     * Create a new Job
     * @param work the lambda to executed later
     */
    @JvmStatic
    fun createJob(work: Job<Lambda>.() -> Unit): Job<Lambda> = MainThreadJob(null, work)

    /**
     * Create a new Job which uses Coroutines
     * @param work the lambda to executed later
     */
    fun createCoroutineJob(scope: CoroutineScope = GlobalScope, work: suspend () -> Unit): Job<SuspendFunction> = SuspendJob(scope, work)

    /** Extension function to find the lifecycle based on the context */
    private fun Context.findLifecycle(): Lifecycle? {
        var context: Context? = this
        while (context != null && context !is LifecycleOwner) {
            context = (context as? ContextWrapper)?.baseContext
        }
        return (context as? LifecycleOwner)?.lifecycle
    }

    /** Interface to access the Job API */
    interface Job<WorkType> {
        /** Execute the work delayed */
        fun doLater(ms: Long)

        /** Execute once a delayed work, which will be canceled by another call of [doNow()] or [doLater(...)] */
        fun doLater(ms: Long, work: WorkType)

        /** Execute the work now */
        fun doNow()
    }

    /** Wrapper to hold a lambda called work which should be executed later or now */
    private class MainThreadJob(
        private val lifecycle: Lifecycle?,
        private val work: Job<Lambda>.() -> Unit
    ) : Job<Lambda> {
        val handler by lazy { ThreadingHelper.createHandler() }
        var lastTask: Task? = null

        /** Execute the work delayed */
        override fun doLater(ms: Long) {
            doLater(ms, Task(lifecycle, this, work))
        }

        override fun doLater(ms: Long, work: () -> Unit) {
            doLater(ms, Task(lifecycle, this) { work() })
        }

        private fun doLater(ms: Long, task: Task) {
            lastTask?.cancel()
            lastTask = task
            handler.postDelayed(task, ms)
        }

        /**
         * Execute the work now. If this is called on the main thread this is executed directly or
         * else posted on the main thread and will be executed on the next run of the main loop.
         */
        override fun doNow() {
            lastTask?.cancel()
            if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != false) {
                if (ThreadingHelper.isOnMainThread) {
                    work()
                } else {
                    val currentTask = Task(lifecycle, this, work)
                    lastTask = currentTask
                    handler.post(currentTask)
                }
            }
        }
    }

    /** Wrapper to hold a suspend function which should be executed with a coroutine */
    private class SuspendJob(
        private val scope: CoroutineScope,
        private val work: SuspendFunction
    ) : Job<SuspendFunction> {
        /** The currently executed coroutine */
        private var coroutineJob: kotlinx.coroutines.Job? = null

        /** Execute the work delayed */
        override fun doLater(ms: Long) {
            coroutineJob?.cancel()
            coroutineJob = scope.launch {
                delay(ms)
                work()
                coroutineJob = null
            }
        }

        /** Execute the work on the given scope now */
        override fun doNow() {
            coroutineJob?.cancel()
            scope.launch {
                work()
                coroutineJob = null
            }
        }

        override fun doLater(ms: Long, work: SuspendFunction) {
            coroutineJob?.cancel()
            scope.launch {
                delay(ms)
                work()
                coroutineJob = null
            }
        }
    }

    /** Holder of the cancelable Job */
    private class Task(
        private val lifecycle: Lifecycle?,
        private val job: Job<Lambda>,
        private val work: Job<Lambda>.() -> Unit
    ) : Runnable {
        private var canceled = false

        /** Cancel the planned execution */
        fun cancel() {
            canceled = true
        }

        override fun run() {
            if (!canceled && lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != false) work(job)
        }
    }
}