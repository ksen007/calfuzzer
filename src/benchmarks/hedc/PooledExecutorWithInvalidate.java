package benchmarks.hedc;

/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * Adopted from 
 * --> EDU.oswego.cs.dl.util.concurrent.PooledExecutor <--
 * --> (Doug Lea)                                      <--
 * 
 * @version $Id: PooledExecutorWithInvalidate.java,v 1.1 2001/03/16 17:55:07 praun Exp $
 * @author Christoph von Praun 
 */

import benchmarks.EDU.oswego.cs.dl.util.concurrent.Channel;
import benchmarks.EDU.oswego.cs.dl.util.concurrent.SynchronousChannel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PooledExecutorWithInvalidate {

    public static String POE01_ = "POE01 - illegal argument %1=%2";

    /**
     * The maximum pool size; used if not otherwise specified.
     * Default value is essentially infinite (Integer.MAX_VALUE)
     */
    public static final int DEFAULT_MAXIMUMPOOLSIZE = Integer.MAX_VALUE;


    /**
     * The minimum pool size; used if not otherwise specified.
     * Default value is 1.
     */
    public static final int DEFAULT_MINIMUMPOOLSIZE = 20;

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

    public PooledExecutorWithInvalidate() {
        this(new SynchronousChannel(), DEFAULT_MAXIMUMPOOLSIZE);
    }


    /**
     * Create a new pool with all default settings except
     * for maximum pool size.
     */

    public PooledExecutorWithInvalidate(int maxPoolSize) {
        this(new SynchronousChannel(), maxPoolSize);
    }

    /**
     * Create a new pool that uses the supplied Channel for queuing,
     * and with all default parameter settings.
     */

    public PooledExecutorWithInvalidate(Channel channel) {
        this(channel, DEFAULT_MAXIMUMPOOLSIZE);
    }

    /**
     * Create a new pool that uses the supplied Channel for queuing,
     * and with all default parameter settings  except
     * for maximum pool size.
     */

    public PooledExecutorWithInvalidate(Channel channel, int maxPoolSize) {
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
        if (newMaximum <= 0) {
            Messages.warn(0, POE01_, "newMaximum", String.valueOf(newMaximum));
            throw new IllegalArgumentException();
        }
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
        if (newMinimum < 0) {
            Messages.warn(0, POE01_, "newMinimum", String.valueOf(newMinimum));
            throw new IllegalArgumentException();
        }
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
    protected void addThread(Task command) {
        if (command.valid) {
            ++poolSize_;
            Worker worker = new Worker(command);
            Thread thread = new Thread(worker);
            command.setThread(thread);
            threads_.put(worker, thread);
            thread.setDaemon(true);
            thread.start();
        }
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
    protected Task getTask() throws InterruptedException {
        long waitTime = getKeepAliveTime();
        if (waitTime >= 0)
            return (Task) (handOff_.poll(waitTime));
        else
            return (Task) (handOff_.take());
    }


    /**
     * Class defining the basic run loop for pooled threads.
     */
    protected class Worker implements Runnable {
        protected Task firstTask_;

        Worker(Task firstTask) {
            firstTask_ = firstTask;
        }

        public void run() {
            Task task = firstTask_;
            try {
                if (task != null)
                    task.run();
            } catch (Exception e) {
            }

            while (getPoolSize() <= getMaximumPoolSize()) { // die if max lowered
                try {
                    task = getTask();
                    if (task != null && task.valid)
                        task.run();
                    else if (task == null)
                        break;
                } catch (Exception e) {
                }
            }
            workerDone(this);
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
    public void execute(Task command) throws InterruptedException {

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
