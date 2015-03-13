/*
  File: At.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  29Aug1998  dl               created initial public version
*/

package benchmarks.EDU.oswego.cs.dl.util.concurrent;
import java.util.Date;

/**
 * A general-purpose timer daemon, vaguely similar in functionality
 * common system-level utilities like at (and the associated crond) in Unix.
 * Objects of this class maintain a single thread and a task queue
 * that may be used to execute Runnable commands in any of three modes --
 * absolute (run at a given time), relative (run after a given delay),
 * and periodic (cyclically run with a given delay).
 * <p>
 * All commands are executed by the single background thread. 
 * The thread is not actually started until the first 
 * request is encountered. Also, if the
 * thread is stopped for any reason, one is started upon the next request.
 * <p>
 * If you would instead like commands run in their own threads, you can
 * use as arguments Runnable commands that start their own threads
 * (or perhaps wrap within ThreadedExecutors). 
 * <p>
 * You can also use multiple
 * daemon objects, each using a different background thread. However,
 * one of the reasons for using a time daemon is to pool together
 * processing of infrequent tasks using a single background thread.
 * <p>
 * Background threads are created using a ThreadFactory. The
 * default factory does <em>not</em>
 * automatically <code>setDaemon</code> status.
 *
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 **/

public class TimeDaemon extends ThreadFactoryUser  {


  /** tasks are maintained in a standard priority queue **/
  protected final Heap heap_ = new Heap(DefaultChannelCapacity.get());


  protected static class TaskNode implements Comparable {
    final Runnable command;   // The command to run
    final long period;        // The cycle period, or -1 if not periodic
    private long timeToRun_;  // The time to run command

    // Cancellation does not immediately remove node, it just
    // sets up lazy deletion bit, so is thrown away when next 
    // encountered in run loop

    private boolean cancelled_ = false;

    // Access to cancellation status and and run time needs sync 
    // since they can be written and read in different threads

    synchronized void setCancelled() { cancelled_ = true; }
    synchronized boolean getCancelled() { return cancelled_; }

    synchronized void setTimeToRun(long w) { timeToRun_ = w; }
    synchronized long getTimeToRun() { return timeToRun_; }
    
    
    public int compareTo(Object other) {
      long a = getTimeToRun();
      long b = ((TaskNode)(other)).getTimeToRun();
      return (a < b)? -1 : ((a == b)? 0 : 1);
    }

    TaskNode(long w, Runnable c, long p) {
      timeToRun_ = w; command = c; period = p;
    }

    TaskNode(long w, Runnable c) {
      timeToRun_ = w; command = c; period = -1;
    }
  }


  /** 
   * Execute the given command at the given time.
   * @param date -- the absolute time to run the command, expressed
   * as a java.util.Date.
   * @param command -- the command to run at the given time.
   * @return taskID -- an opaque reference that can be used to cancel execution request
   **/
  public Object executeAt(Date date, Runnable command) {
    TaskNode task = new TaskNode(date.getTime(), command); 
    heap_.insert(task);
    restart();
    return task;
  }

  /** 
   * Excecute the given command after waiting for the given delay.
   * @param millisecondsToDelay -- the number of milliseconds
   * from now to run the command.
   * @param command -- the command to run after the delay.
   * @return taskID -- an opaque reference that can be used to cancel execution request
   * <p>
   * <b>Sample Usage.</b>
   * You can use a TimeDaemon to arrange timeout callbacks to break out
   * of stuck IO. For example (code sketch):
   * <pre>
   * class X {   ...
   * 
   *   TimeDaemon timer = ...
   *   Thread readerThread;
   *   FileInputStream datafile;
   * 
   *   void startReadThread() {
   *     datafile = new FileInputStream("data", ...);
   * 
   *     readerThread = new Thread(new Runnable() {
   *      public void run() {
   *        for(;;) {
   *          // try to gracefully exit before blocking
   *         if (Thread.currentThread().isInterrupted()) {
   *           quietlyWrapUpAndReturn();
   *         }
   *         else {
   *           try {
   *             int c = datafile.read();
   *             if (c == -1) break;
   *             else process(c);
   *           }
   *           catch (IOException ex) {
   *            cleanup();
   *            return;
   *          }
   *       }
   *     } };
   *
   *    readerThread.start();
   *
   *    // establish callback to cancel after 60 seconds
   *    timer.executeAfterDelay(60000, new Runnable() {
   *      readerThread.interrupt();    // try to interrupt thread
   *      datafile.close(); // force thread to lose its input file 
   *    });
   *   } 
   * }
   * </pre>
   **/
  public Object executeAfterDelay(long millisecondsToDelay, Runnable command) {
    long runtime = System.currentTimeMillis() + millisecondsToDelay;
    TaskNode task = new TaskNode(runtime, command);
    heap_.insert(task);
    restart();
    return task;
  }

  /** 
   * Execute the given command every <code>period</code> milliseconds.
   * If <code>startNow</code> is true, execution begins immediately,
   * otherwise, it begins after the first <code>period</code> delay.
   * @param period -- the period, in milliseconds. Periods are
   *  measured from start-of-task to the next start-of-task. It is
   * generally a bad idea to use a period that is shorter than 
   * the expected task duration.
   * @param command -- the command to run at each cycle
   * @param startNow -- true if the cycle should start with execution
   * of the task now. Otherwise, the cycle starts with a delay of
   * <code>period</code> milliseconds.
   * @exception IllegalArgumentException if period less than or equal to zero.
   * @return taskID -- an opaque reference that can be used to cancel execution request
   **/
  public Object executePeriodically(long period,
                                    Runnable command, 
                                    boolean startNow) {

    if (period <= 0) throw new IllegalArgumentException();

    long firstTime = System.currentTimeMillis();
    if (!startNow) firstTime += period;

    TaskNode task = new TaskNode(firstTime, command, period); 
    heap_.insert(task);
    restart();
    return task;
  }

  /** 
   * Cancel a scheduled task. The task will be cancelled
   * upon the <em>next</em> opportunity to run it. This has no effect if
   * this is a one-shot task that has already executed.
   * If an execution is in progress, it will complete normally,
   * but if it is a periodic task, future iterations are cancelled. 
   * @param taskID -- a task reference returned by one of
   * the execute commands
   * @exception ClassCastException if the taskID argument is not 
   * of the type returned by an execute command.
   **/
  public static void cancel(Object taskID) {
    ((TaskNode)taskID).setCancelled();
  }
   

  /** The thread used to process commands **/
  protected Thread thread_;

  
  /**
   * Return the thread being used to process commands, or
   * null if there is no such thread. You can use this
   * to invoke any special methods on the thread, for
   * example, to interrupt it.
   **/
  public synchronized Thread getThread() { 
    return thread_;
  }

  /** set thread_ to null to indicate termination **/
  protected synchronized void clearThread() {
    thread_ = null;
  }

  /**
   * Start (or restart) a thread to process commands, or wake
   * up an existing thread if one is already running.
   **/

  protected synchronized void restart() {
    if (thread_ == null) {
      thread_ = threadFactory_.newThread(runLoop_);
      thread_.start();
    }
    else
      notify();
  }


  /**
   * Cancel all tasks and interrupt the background thread executing
   * the current task, if any.
   * (A new background thread will be started if new execution
   * requests are encountered.)
   **/
  public synchronized void shutDown() {
    heap_.clear();
    if (thread_ != null) 
      thread_.interrupt();
  }

  /** Return the next task to execute, or null if thread is interrupted **/
  protected synchronized TaskNode nextTask() {

    try {

      for (;;) {

        if (Thread.interrupted()) return null;

        long waitTime = 0; // assume indefinite wait below

        // Using peek simplifies dealing with spurious wakeups

        TaskNode task = (TaskNode)(heap_.peek());

        if (task != null) {
          long now = System.currentTimeMillis();
          long when = task.getTimeToRun();

          if (when > now) // false alarm wakeup
            waitTime = when - now;

          else {
            // Even if a new task was inserted since peek
            // the current least must be right one to return
            task = (TaskNode)(heap_.extract());

            // Task cannot be null here, but could in plausible subclasses
            if (task != null) {

              // Skip if cancelled
              if (task.getCancelled()) {
                waitTime = -1; // bypass the wait below
              }

              else {
                // If periodic, requeue 
                if (task.period > 0) {
                  task.setTimeToRun(now + task.period);
                  heap_.insert(task);
                }
                
                return task;
              }
            }
          }
        }
        if (waitTime >= 0)
          wait(waitTime);
      }
    }
    catch (InterruptedException ex) {
      return null;
    }
  }

  /**
   * The runloop is isolated in its own Runnable class
   * just so that the main 
   * class need not implement Runnable,  which would
   * allow others to directly invoke run, which would
   * never make sense here.
   **/

  protected class RunLoop implements Runnable {
    public void run() {
      try {
        for (;;) {
          TaskNode task = nextTask();
          if (task != null) 
            task.command.run();
          else
            break;
        }
      }
      finally {
        clearThread();
      }
    }
  }

  protected final RunLoop runLoop_;

  /** 
   * Create a new TimeDaemon 
   **/

  public TimeDaemon() {
    runLoop_ = new RunLoop();
  }

    

}
