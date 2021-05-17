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
    fun createLifeCycleAwareJob(lifecycle: Lifecycle, work:()->Unit) = Job(lifecycle, work)

    /**
     * Create a new Job
     * @param work the lambda to executed later
     */
    fun createJob(work:()->Unit) = Job(null, work)

    /** Wrapper to hold a lambda called work which should be executed later or now */
    class Job internal constructor(private val lifecycle: Lifecycle?, private val work:()->Unit) {
        val handler by lazy { Handler(Looper.getMainLooper()) }
        var lastTask: Task? = null

        /** Execute the work dalayed */
        fun doLater(ms: Long) {
            lastTask?.cancel()
            val currentTask = Task(lifecycle, work)
            lastTask = currentTask
            handler.postDelayed(currentTask, ms)
        }

        /**
         * Execute the work now. If this is called on the main thread this is executed directly or
         * else posted on the main thread and will be executed on the next run of the main loop.
         */
        fun doNow() {
            lastTask?.cancel()
            if(Looper.getMainLooper().thread == Thread.currentThread()) {
                work()
            } else {
                handler.post {
                    work()
                }
            }
        }
    }

    /**
     * Private wrapper of a work called Task, which will be executed lifecycle aware as requested
     * or not while creating the Job
     */
    private class Task(private val lifecycle: Lifecycle?, private val work:()->Unit): Runnable {
        /** Flag that this Task was cancled */
        private var canceled = false

        /** Mark this task as cancled */
        fun cancel() {
            canceled = true
        }

        /**
         * Execute the work when the Task is not cancled and the lifececle is at least resumed if a
         * lifecycle it set
         */
        override fun run() {
            if(!canceled && lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != false) work()
        }
    }
}