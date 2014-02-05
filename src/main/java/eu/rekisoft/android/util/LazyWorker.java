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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Utility for executing delayed tasks, which can been canceled. It you add a task multiple times it
 * will been executed after the least desired delay.
 *
 * @author René Kilczan
 * @version 1.0
 * @copyright This code is licensed under the Rekisoft Public License.<br/>
 * See http://www.rekisoft.eu/licenses/rkspl.html for more informations.
 */
public class LazyWorker {
    /**
     * The pool where the tasks are executed.
     */
    protected ScheduledThreadPoolExecutor exec;
    /**
     * The stored outstanding tasks.
     */
    protected Map<Runnable, ScheduledFuture<?>> tasks;
    /**
     * The sequence number of the LazyWorker instances.
     */
    protected static int seq = 0;

    /**
     * The saved singleton instance of the LazyWorker.
     */
    protected static LazyWorker instance;

    /**
     * @return a instance of the LazyWorker.
     */
    public static synchronized LazyWorker getSharedInstance() {
        if(instance == null) {
            instance = new LazyWorker();
        }
        return instance;
    }

    /**
     * Creates a new instance of the LazyWorker.
     */
    public LazyWorker() {
        exec = new ScheduledThreadPoolExecutor(1);
        exec.setThreadFactory(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("LazyWorker #" + ++seq);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            }
        });
        tasks = new HashMap<Runnable, ScheduledFuture<?>>();
    }

    /**
     * Creates a new instance of LazyWorker with the option to add a task directly in the
     * constructor with a delay.
     *
     * @param task  The Runnable which should been executed delayed.
     * @param delay The delay after that the task should been executed.
     */
    public LazyWorker(Runnable task, int delay) {
        this();
        doLater(task, delay);
    }

    /**
     * Executes a task after a given delay another call of this method will set a new time of
     * execution.
     *
     * @param task  The Runnable which should been executed delayed.
     * @param delay The delay after that the task should been executed.
     */
    public void doLater(Runnable task, int delay) {
        if(task == null)
            throw new NullPointerException("task was null!");
        if(tasks.containsKey(task)) {
            tasks.get(task).cancel(true);
        }
        tasks.put(task, exec.schedule(task, delay, TimeUnit.MILLISECONDS));
    }

    /**
     * Cancel a given task which is pending.
     *
     * @param task The task which should been canceled.
     */
    public void cancelTask(Runnable task) {
        if(tasks.containsKey(task)) {
            tasks.get(task).cancel(true);
        }
    }

    /**
     * Checks if a given task is already pending.
     *
     * @param task The task which should been checked.
     * @return <code>true</code> if the task is pending.
     */
    public boolean hasTaskPending(Runnable task) {
        return tasks.containsKey(task);
    }

    /**
     * Shutdowns the LazyWorker all pending tasks will been canceled with this call.
     */
    public void shutdown() {
        exec.shutdown();
    }
}