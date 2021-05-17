/**
 * @copyright
 * This code is licensed under the Rekisoft Public License.
 * See http://www.rekisoft.eu/licenses/rkspl.html for more informations.
 */
/**
 * @package eu.rekisoft.android.util
 * This package contains utilities provided by [rekisoft.eu](http://rekisoft.eu/). 
 */
package eu.rekisoft.android.util;

// TODO add missing imports

/**
 * Utility for executing delayed tasks, which can been canceled. It you add a task multiple times it
 * will been executed after the least desired delay.
 *
 * @author René Kilczan
 * @version 2.0
 * @copyright This code is licensed under the Rekisoft Public License.<br/>
 * See https://www.rekisoft.eu/licenses/rkspl.html for more informations.
 */
object LazyWorker {
    /**
     * Create a life cycle aware new Job
     * @param lifecycle the lifecycle of the owner
     * @param work the lambda to executed later
     */
    fun createLifeCycleAwareJob(lifecycle: Lifecycle, work: () -> Unit): Job = MainThreadJob(lifecycle, work)


    /**
     * Create a new Job
     * @param work the lambda to executed later
     */
    fun createJob(work: () -> Unit): Job = MainThreadJob(null, work)

    /**
     * Create a new Job which uses couroutines
     * @param work the lambda to executed later
     */
    fun createCoroutineJob(scope: CoroutineScope = GlobalScope, work: suspend () -> Unit): Job = SuspendJob(scope, work)

    /** Interface to access the Job API */
    interface Job {
        /** Execute the work dalayed */
        fun doLater(ms: Long)
        /** Execute the work now */
        fun doNow()
    }

    /** Wrapper to hold a lambda called work which should be executed later or now */
    private class MainThreadJob(private val lifecycle: Lifecycle?, private val work: () -> Unit) : Job {
        val handler by lazy { Handler(Looper.getMainLooper()) }
        var lastTask: Task? = null

        /** Execute the work dalayed */
        override fun doLater(ms: Long) {
            lastTask?.cancel()
            val currentTask = Task(lifecycle, work)
            lastTask = currentTask
            handler.postDelayed(currentTask, ms)
        }

        /**
         * Execute the work now. If this is called on the main thread this is executed directly or
         * else posted on the main thread and will be executed on the next run of the main loop.
         */
        override fun doNow() {
            lastTask?.cancel()
            if (Looper.getMainLooper().thread == Thread.currentThread()) {
                work()
            } else {
                handler.post {
                    work()
                }
            }
        }
    }

    /** Wrapper to hold a lambda which should be executed with a coroutine */
    private class SuspendJob(private val scope: CoroutineScope, private val work: suspend () -> Unit) : Job {
        /** The currently executed coroutine */
        private var coroutineJob: kotlinx.coroutines.Job? = null

        /** Execute the work dalayed */
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
    }

    private class Task(private val lifecycle: Lifecycle?, private val work: () -> Unit) : Runnable {
        private var canceled = false
        fun cancel() {
            canceled = true
        }

        override fun run() {
            if (!canceled && lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != false) work()
        }
    }
}