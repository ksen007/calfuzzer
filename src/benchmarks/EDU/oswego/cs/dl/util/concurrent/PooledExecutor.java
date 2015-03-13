/*
  File: PooledExecutor.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  19Jun1998  dl               Create public version
  29aug1998  dl               rely on ThreadFactoryUser, 
                              remove ThreadGroup-based methods
                              adjusted locking policies
   3mar1999  dl               Worker threads sense decreases in pool size
  31mar1999  dl               Allow supplied channel in constructor;
                              add methods createThreads, drain
  15may1999  dl               Allow infinite keepalives
  21oct1999  dl               add minimumPoolSize methods
*/

package benchmarks.EDU.oswego.cs.dl.util.concurrent;

import java.util.*;

/**
 * A tunable, extensible thread pool class. The main supported public
 * method is <code>execute(Runnable command)</code>, which
 * can be called instead of directly creating threads
 * to execute commands.
 * <p/>
 * Thread pools can be useful for several, usually intertwined
 * reasons:
 * <ul>
 * <li> To bound resource use. A limit can be
 * placed on the maximum number of simultaneously executing
 * threads.
 * <li> To manage concurrency levels. A targeted number of
 * threads can be allowed to execute simultaneously.
 * <li> To manage a set of threads performing related tasks.
 * <li> To minimize overhead, by reusing previously constructed
 * Thread objects rather than creating new ones.
 * (Note however that pools are hardly ever cure-alls
 * for performance problems associated with thread construction,
 * especially on JVMs that themselves internally pool or
 * recycle threads.)
 * </ul>
 * These goals introduce a number of policy parameters
 * that are encapsulated in this class. All of these parameters
 * have defaults and are tunable,
 * either via get/set methods, or, in cases where decisions
 * should hold across lifetimes, via methods that can be easily
 * overridden in subclasses.  The main, most
 * commonly set parameters can be established in constructors.
 * Policy choices across these dimensions can and do interact.
 * Be careful. See the usage examples below.
 * <p/>
 * <dl>
 * <dt> Queueing
 * <dd> By default, this pool uses queueless synchronous channels to
 * to hand off work to threads. This is a safe, conservative
 * policy that avoids lockups
 * when handling sets of requests that might have
 * internal dependencies. (In these cases, queuing one task
 * could lock up another that
 * would be able to continue if the queued task were to run.)
 * If you are sure that this cannot happen, then you can
 * instead supply a queue of some sort (for example,
 * a BoundedBuffer or LinkedQueue) in the constructor.
 * This will cause new commands to be queued in cases where
 * all MaximumPoolSize threads are busy. Queues are sometimes
 * appropriate when each task is completely independent of
 * others, so tasks cannot affect each others execution.
 * For example, in an http server.
 * <p/>
 * When given a choice, this pool always prefers adding
 * a new thread rather than queueing if there are currently
 * fewer than the
 * current getMinimumPoolSize threads running,
 * but otherwise always prefers queuing
 * a request rather than adding a new thread. Thus, if you use an
 * unbounded buffer, you will never have more than
 * getMinimumPoolSize threads running. (Since the
 * default minimumPoolSize is one, you will probably want to
 * explicitly setMinimumPoolSize.)
 * <p/>
 * While queuing can be useful in smoothing out transient
 * bursts of requests, especially in socket-based services,
 * it is not very well behaved
 * when commands continue to arrive
 * on average faster than they can be processed.
 * Using bounds for both the queue and the
 * pool size, along with run-when-blocked policy
 * is often a reasonable response to such possibilities.
 * <p/>
 * Queue sizes and maximum pool sizes can often be traded
 * off for each other. Using large queues and small pools
 * minimizes CPU usage, OS resources, and context-switching overhead,
 * but can lead to artifically low
 * throughput. Especially if tasks frequently block (for example if
 * they are I/O bound),  a JVM and underlying OS may be able
 * to schedule time for more threads than you otherwise
 * allow. Use of small queues or queueless handoffs generally
 * requires larger pool sizes, which keeps CPUs busier
 * but may encounter unacceptable scheduling overhead,
 * which also decreases throughput.
 * <p/>
 * <dt> Maximum Pool size
 * <dd> The maximum number of threads to use, when needed.
 * The pool does not by default preallocate threads.
 * Instead, a thread is created, if
 * necessary and if there are fewer than the maximum, only
 * when an <code>execute</code> request arrives.
 * The default value is (for all practical purposes) infinite --
 * <code>Integer.MAX_VALUE</code>, so should
 * be set in the constructor or the set method unless you
 * are just using the pool to minimize construction overhead.
 * Because task handoffs to idle worker threads require synchronization
 * that in turn relies on JVM scheduling policies to ensure progress,
 * it is possible that a new thread will be created even though
 * an existing worker thread has just become idle but has not progressed
 * to the point at which it can accept a new task. This phenomenon
 * tends to occur on some JVMs when bursts of short tasks
 * are executed.
 * <p/>
 * <dt> Minimum Pool size
 * <dd> The minimum number of threads to use, when needed (default 1).
 * When a new request is received, and fewer than the
 * minimum number of threads are running, a new thread is
 * always created to handle the request even if other
 * worker threads are idly waiting for work. Otherwise,
 * a new thread is created only if there are fewer than the
 * maximum and the request cannot immediately be queued.
 * <p/>
 * <dt> Preallocation
 * <dd> You can override lazy thread construction
 * policies via method createThreads, which establishes
 * a given number of warm threads. Be aware that these preallocated
 * threads will time out and die (and later be replaced
 * with others if needed) if not used within the
 * keep-alive time window. If you use preallocation, you
 * probably want to increase the keepalive time.
 * The difference between setMinimumPoolSize and createThreads
 * is that createThreads immediately establishes threads,
 * while setting the minimum pool size waits until requests
 * arrive.
 * <p/>
 * <dt> Keep-alive time
 * <dd> If the pool maintained references to a fixed set of
 * threads in the pool,
 * then it would impede garbage collection of otherwise
 * idle threads. This would defeat the resource-management
 * aspects of pools. One solution would be to use weak references.
 * However, this would impose costly and difficult
 * synchronization issues.
 * Instead, threads are simply allowed to terminate
 * and thus be GCable if they have been idle for the
 * given keep-alive time.  The value of this parameter
 * represents a trade-off between GCability and construction
 * time. In most current Java VMs, thread
 * construction and cleanup overhead
 * is on the order of milliseconds. The
 * default keep-alive value is one minute, which means that
 * the time needed to construct and then GC a thread is expended
 * at most once per minute.
 * <p/>
 * To establish worker threads permanently, use a <em>negative</em>
 * argument to setKeepAliveTime.
 * <p/>
 * <dt> Blocked execution policy
 * <dd> If the maximum pool size or queue size is
 * bounded, then it is possible for incoming <code>execute</code>
 * requests to block. There are three supported policies
 * for handling this problem, and mechanics (based on
 * the Strategy Object pattern) to allow
 * others in subclasses: <p>
 * <dl>
 * <dt> Run (the default)
 * <dd> The thread making the <code>execute</code> request
 * runs the task itself.
 * This policy helps guard against lockup.
 * <dt> Wait
 * <dd> Wait until a thread becomes available.
 * <dt> Discard
 * <dd> Throw away the current request and return.
 * </dl>
 * Other plausible policies include raising the maximum pool
 * size after checking with some other objects that this is OK.
 * <p/>
 * (Again, these cases can never occur if
 * the maximum pool size is unbounded or the queue is unbounded.
 * In these cases you instead face potential resource exhaustion.)
 * The execute method does not throw any checked exceptions
 * in any of these cases since any errors associated with them
 * must normally be dealt with via handlers or callbacks. (Although
 * in some cases, these might be associated with throwing
 * unchecked exceptions.)
 * You may wish to add special implementations
 * even if you choose one of the listed policies. For example,
 * the supplied Discard policy does not inform the caller of
 * the drop. You could add your own version that does so.
 * Since choice of policies is normally a system-wide decision,
 * selecting a policy affects all calls to <code>execute</code>.
 * If for some reason you would instead like to make per-call decisions,
 * you could add variant versions of the <code>execute</code>
 * method (for example, <code>executeIfWouldNotBlock</code>) in
 * subclasses.
 * <p/>
 * <dt> Thread construction parameters
 * <dd>
 * A settable ThreadFactory establishes each new thread.
 * By default, it merely generates a new instance of
 * class Thread, but can be changed to use a
 * Thread subclass, to set priorities, ThreadLocals, etc.
 * <p/>
 * <dt> Interruption policy
 * <dd>  Worker threads check for interruption after  processing
 * each command, and terminate upon interruption.
 * Fresh threads will replace
 * them if needed. Thus, new tasks will not start
 * out in an interrupted state due to an uncleared interruption in a
 * previous task. Also, unprocessed commands are never dropped
 * upon interruption. It would conceptually suffice simply to clear
 * interruption between tasks, but implementation characteristics
 * of interruption-based methods are uncertain enough to warrant
 * this conservative strategy. It is a good idea to be
 * equally conservative
 * in your code for the tasks running within pools. Normally, before
 * shutting down a pool via method interruptAll, you should make
 * sure that all clients of the pool are themselves terminated,
 * in order to prevent hanging or lost commands. Additionally,
 * if you are using some form of queuing, you may wish to
 * call method drain() to remove (and return)
 * unprocessed commands from
 * the queue after shutting down
 * the pool and its clients. If you need to be sure these
 * commands are processed, you can then run() each of the
 * commands in the list returned by drain().
 * <p/>
 * </dl>
 * <p/>
 * <b>Usage examples.</b>
 * <p/>
 * Probably the most common use of pools is in statics or singletons accessible
 * from a number of classes in a package;
 * for example:
 * <pre>
 * class MyPool {
 *   // initialize to use a maximum of 8 threads.
 *   static PooledExecutor pool = new PooledExecutor(8);
 * }
 * </pre>
 * Here are some sample variants in initialization:
 * <ol>
 * <li> Using a bounded buffer of 10 tasks, at least 4 threads (started only
 * when needed due to incoming requests), but allowing
 * up to 100 threads if the buffer gets full.
 * <pre>
 *        pool = new PooledExecutor(new BoundedBuffer(10), 100);
 *        pool.setMinimumPoolSize(4);
 *     </pre>
 * <li> Same as (1), except pre-start 9 threads, allowing them to
 * die if they are not used for five minutes.
 * <pre>
 *        pool = new PooledExecutor(new BoundedBuffer(10), 100);
 *        pool.setMinimumPoolSize(4);
 *        pool.setKeepAliveTime(1000 * 60 * 5);
 *        pool.createThreads(9);
 *     </pre>
 * <li> Same as (2) except clients block if both the buffer is full and
 * all 100 threads are busy:
 * <pre>
 *        pool = new PooledExecutor(new BoundedBuffer(10), 100);
 *        pool.setMinimumPoolSize(4);
 *        pool.setKeepAliveTime(1000 * 60 * 5);
 *        pool.waitWhenBlocked();
 *        pool.createThreads(9);
 *     </pre>
 * <li> An unbounded queue serviced by exactly 5 threads:
 * <pre>
 *        pool = new PooledExecutor(new LinkedQueue());
 *        pool.setKeepAliveTime(-1); // live forever
 *        pool.createThreads(5);
 *     </pre>
 * </ol>
 * <p/>
 * <p/>
 * <b>Usage notes.</b>
 * <p/>
 * Pools do not mesh well with using thread-specific storage via
 * java.lang.ThreadLocal.  ThreadLocal relies on the identity of a
 * thread executing a particular task. Pools use the same thread to
 * perform different tasks.
 * <p/>
 * If you need a policy not handled by the parameters in this class
 * consider writing a subclass.
 * <p/>
 * Version note: Previous versions of this class relied on ThreadGroups
 * for aggregate control. This has been removed, and the method
 * interruptAll added, to avoid differences in behavior across JVMs.
 * <p/>
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 */

public class PooledExecutor extends ThreadFactoryUser implements Executor {

    /**
     * The maximum pool size; used if not otherwise specified.
     * Default value is essentially infinite (Integer.MAX_VALUE)
     */
    public static final int DEFAULT_MAXIMUMPOOLSIZE = Integer.MAX_VALUE;


    /**
     * The minimum pool size; used if not otherwise specified.
     * Default value is 1.
     */
    public static final int DEFAULT_MINIMUMPOOLSIZE = 1;

    /**
     * The maximum time to keep worker threads alive waiting for new
     * tasks; used if not otherwise specified. Default
     * value is one minute (60000 milliseconds).
     */
    public static final long DEFAULT_KEEPALIVETIME = 60 * 1000;

    // Bounds declared as volatile since there is no point
    // in trying to carefully synchronize responses to changes
    // in value.

    protected volatile int maximumPoolSize_ = DEFAULT_MAXIMUMPOOLSIZE;
    protected volatile int minimumPoolSize_ = DEFAULT_MINIMUMPOOLSIZE;

    protected long keepAliveTime_ = DEFAULT_KEEPALIVETIME;

    /**
     * The channel is used to hand off the command
     * to a thread in the pool
     */
    protected final Channel handOff_;

    /**
     * Lock used for protecting poolSize_ and threads_ map *
     */
    protected Object poolLock_ = new Object();

    /**
     * Current pool size. Relies on poolLock_ for all locking.
     * But is also volatile to allow simpler checking inside
     * worker thread runloop.
     */

    protected volatile int poolSize_ = 0;

    /**
     * The set of active threads,
     * declared as a map from workers to their threads.
     * This is needed by the interruptAll method.
     * It may also be useful in subclasses that need to perform
     * other thread management chores.
     * All operations on the Map should be done holding
     * synchronization on poolLock.
     */
    protected final Map threads_;

    /**
     * Create a new pool with all default settings
     */

    public PooledExecutor() {
        this(new SynchronousChannel(), DEFAULT_MAXIMUMPOOLSIZE);
    }


    /**
     * Create a new pool with all default settings except
     * for maximum pool size.
     */

    public PooledExecutor(int maxPoolSize) {
        this(new SynchronousChannel(), maxPoolSize);
    }

    /**
     * Create a new pool that uses the supplied Channel for queuing,
     * and with all default parameter settings.
     */

    public PooledExecutor(Channel channel) {
        this(channel, DEFAULT_MAXIMUMPOOLSIZE);
    }

    /**
     * Create a new pool that uses the supplied Channel for queuing,
     * and with all default parameter settings  except
     * for maximum pool size.
     */

    public PooledExecutor(Channel channel, int maxPoolSize) {
        maximumPoolSize_ = maxPoolSize;
        handOff_ = channel;
        runWhenBlocked();
        threads_ = new HashMap();
    }

    /**
     * Return the maximum number of threads to simultaneously execute
     * New requests will be handled according to the current
     * blocking policy once this limit is exceeded.
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize_;
    }

    /**
     * Set the maximum number of threads to use. Decreasing
     * the pool size will not immediately  kill existing threads,
     * but they may later die when idle.
     *
     * @throws IllegalArgumentException if less or equal to zero.
     *                                  (It is
     *                                  not considered an error to set the maximum to be less than than
     *                                  the minimum. However, in this case there are no guarantees
     *                                  about behavior.)
     */
    public void setMaximumPoolSize(int newMaximum) {
        if (newMaximum <= 0) throw new IllegalArgumentException();
        maximumPoolSize_ = newMaximum;
    }

    /**
     * Return the minimum number of threads to simultaneously execute.
     * (Default value is 1).
     * If fewer than the mininum number are running upon reception
     * of a new request, a new thread is started to handle this request.
     */
    public int getMinimumPoolSize() {
        return minimumPoolSize_;
    }

    /**
     * Set the minimum number of threads to use.
     *
     * @throws IllegalArgumentException if less than zero. (It is
     *                                  not considered an error to set the minimum to be greater than
     *                                  the maximum. However, in this case there are no guarantees
     *                                  about behavior.)
     */
    public void setMinimumPoolSize(int newMinimum) {
        if (newMinimum < 0) throw new IllegalArgumentException();
        minimumPoolSize_ = newMinimum;
    }


    /**
     * Return the current number of active threads in the pool.
     * This number is just a snaphot, and may change immediately
     * upon returning
     */
    public int getPoolSize() {
        return poolSize_;
    }


    /**
     * Create and start a thread to handle a new command.
     * Observer only when holding poolLock.
     */
    protected void addThread(Runnable command) {
        ++poolSize_;
        Worker worker = new Worker(command);
        Thread thread = getThreadFactory().newThread(worker);
        threads_.put(worker, thread);
        thread.start();
    }

    /**
     * Create and start up to numberOfThreads threads in the pool.
     * Return the number created. This may be less than the
     * number requested if creating more would exceed maximum
     * pool size bound.
     */
    public int createThreads(int numberOfThreads) {
        int ncreated = 0;
        for (int i = 0; i < numberOfThreads; ++i) {
            synchronized (poolLock_) {
                if (getPoolSize() < getMaximumPoolSize()) {
                    ++ncreated;
                    addThread(null);
                } else
                    break;
            }
        }
        return ncreated;
    }

    /**
     * Interrupt all threads in the pool, causing them all
     * to terminate. Assuming that executed tasks do not
     * disable (clear) interruptions, each thread will terminate after
     * processing its current task. Threads will terminate
     * sooner if the executed tasks themselves respond to
     * interrupts.
     */

    public void interruptAll() {
        // Synchronized to avoid concurrentModification exceptions

        synchronized (poolLock_) {
            for (Iterator it = threads_.values().iterator(); it.hasNext();) {
                Thread t = (Thread) (it.next());
                t.interrupt();
            }
        }

    }

    /**
     * Remove all unprocessed tasks from pool queue, and
     * return them in a java.util.List. It should normally be used only
     * when there are not any active clients of the pool (otherwise
     * you face the possibility that the method will loop
     * pulling out tasks as clients are putting them in.)
     * This method can be useful after
     * shutting down a pool (via interruptAll) to determine
     * whether there are any pending tasks that were not processed.
     * You can then, for example execute all unprocessed commands
     * via code along the lines of:
     * <pre>
     *   List tasks = pool.drain();
     *   for (Iterator it = tasks.iterator(); it.hasNext();)
     *     ( (Runnable)(it.next()) ).run();
     * </pre>
     */
    public List drain() {
        boolean wasInterrupted = false;
        Vector tasks = new Vector();
        for (; ;) {
            try {
                Object x = handOff_.poll(0);
                if (x == null)
                    break;
                else
                    tasks.addElement(x);
            }
            catch (InterruptedException ex) {
                wasInterrupted = true; // postpone re-interrupt until drained
            }
        }
        if (wasInterrupted) Thread.currentThread().interrupt();
        return tasks;
    }


    /**
     * Return the number of milliseconds to keep threads
     * alive waiting for new commands. A negative value
     * means to wait forever. A zero value means not to wait
     * at all.
     */
    public synchronized long getKeepAliveTime() {
        return keepAliveTime_;
    }

    /**
     * Set the number of milliseconds to keep threads
     * alive waiting for new commands. A negative value
     * means to wait forever. A zero value means not to wait
     * at all.
     */
    public synchronized void setKeepAliveTime(long msecs) {
        keepAliveTime_ = msecs;
    }

    /**
     * Called upon termination of worker thread *
     */
    protected void workerDone(Worker w) {
        synchronized (poolLock_) {
            --poolSize_;
            threads_.remove(w);
        }
    }

    /**
     * get a task from the handoff queue *
     */
    protected Runnable getTask() throws InterruptedException {
        long waitTime = getKeepAliveTime();
        if (waitTime >= 0)
            return (Runnable) (handOff_.poll(waitTime));
        else
            return (Runnable) (handOff_.take());
    }


    /**
     * Class defining the basic run loop for pooled threads.
     */
    protected class Worker implements Runnable {
        protected Runnable firstTask_;

        Worker(Runnable firstTask) {
            firstTask_ = firstTask;
        }

        public void run() {
            try {
                Runnable task = firstTask_;
                firstTask_ = null;
                if (task != null)
                    task.run();

                while (getPoolSize() <= getMaximumPoolSize()) { // die if max lowered
                    task = getTask();
                    if (task != null)
                        task.run();
                    else
                        break;
                }

            }
            catch (InterruptedException ex) {
            } // fall through
            finally {
                workerDone(this);
            }
        }
    }


    /**
     * Class for actions to take when execute() blocks. Uses Strategy
     * pattern to represent different actions. You can add more
     * in subclasses, and/or create subclasses of these. If so,
     * you will also want to add or modify the corresponding methods that
     * set the current blockedExectionHandler_.
     */

    protected abstract class BlockedExecutionHandler {
        /**
         * Return true if successfully handled so, execute should terminate;
         * else return false if execute loop should be retried
         */
        abstract boolean blockedAction(Runnable command);
    }

    /**
     * Class defining Run action *
     */
    protected class RunWhenBlocked extends BlockedExecutionHandler {
        protected boolean blockedAction(Runnable command) {
            command.run();
            return true;
        }
    }

    /**
     * Class defining Wait action *
     */
    protected class WaitWhenBlocked extends BlockedExecutionHandler {
        protected boolean blockedAction(Runnable command) {
            try {
                handOff_.put(command);
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); // propagate
            }
            return true;
        }
    }

    /**
     * Class defining Discard action *
     */
    protected class DiscardWhenBlocked extends BlockedExecutionHandler {
        protected boolean blockedAction(Runnable command) {
            return true;
        }
    }

    /**
     * The current handler *
     */
    protected BlockedExecutionHandler blockedExecutionHandler_;

    /**
     * Get the handler for blocked execution *
     */
    protected synchronized BlockedExecutionHandler getBlockedExecutionHandler() {
        return blockedExecutionHandler_;
    }


    /**
     * Set the policy for blocked execution to be that
     * the current thread executes the command if
     * there are no available threads in the pool.
     */
    public synchronized void runWhenBlocked() {
        blockedExecutionHandler_ = new RunWhenBlocked();
    }

    /**
     * Set the policy for blocked execution to be to
     * wait until a thread is available.
     */
    public synchronized void waitWhenBlocked() {
        blockedExecutionHandler_ = new WaitWhenBlocked();
    }

    /**
     * Set the policy for blocked execution to be to
     * return without executing the request
     */
    public synchronized void discardWhenBlocked() {
        blockedExecutionHandler_ = new DiscardWhenBlocked();
    }


    /**
     * Arrange for the given command to be executed by a thread in this pool.
     * The method normally returns when the command has been handed off
     * for (possibly later) execution.
     */
    public void execute(Runnable command) throws InterruptedException {

        for (; ;) {

            synchronized (poolLock_) {

                // Ensure minimum number of threads
                if (getPoolSize() < getMinimumPoolSize()) {
                    addThread(command);
                    return;
                }

                // Try to give to existing thread
                if (handOff_.offer(command, 0)) {
                    return;
                }

                // Try to add a new thread to pool
                if (getPoolSize() < getMaximumPoolSize()) {
                    addThread(command);
                    return;
                }

            }

            // Cannot hand off and cannot create -- ask for help
            if (getBlockedExecutionHandler().blockedAction(command)) {
                return;
            }
        }

    }

}
