/*
  File: SynchronousChannel.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  11Jun1998  dl               Create public version
  17Jul1998  dl               Disabled direct semaphore permit check
  31Jul1998  dl               Replaced main algorithm with one with
                              better scaling and fairness properties.
  25aug1998  dl               added peek
*/

package benchmarks.EDU.oswego.cs.dl.util.concurrent;

/**
 * [<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>] <p>
 *  A rendezvous channel, similar to those used in CSP and Ada. 
 *  Each put must wait for a take, and vice versa.
 * <p>
 * Synchronous channels are well suited for handoff designs, in
 * which an object running in one thread must synch up with 
 * an object running in another thread in order to hand it
 * some information, event, or task. This implementation
 * uses WaiterPreferenceSemaphores to help avoid infinite overtaking
 * among multiple threads trying to perform puts or takes.
 * <p>
 * If you only need threads to synch up without exchanging information,
 * consider using a Barrier. If you need bidirectional exchanges,
 * consider using a Rendezvous.
 * <p>
 * For a usage example, see the implementation of PooledExecutor
 * <p>
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * @see CyclicBarrier
 * @see Rendezvous
 * @see PooledExecutor
**/

public class SynchronousChannel implements BoundedChannel {


  /** 
   * Create a new channel.
   **/
  public SynchronousChannel() { }


  /**
   * @return zero --
   * Synchronous channels have no internal capacity.
   **/
  public int capacity() { return 0; }

  /** 
   * The item currently being transferred.
   **/
  protected Object item_ = null;

  /** Set the item in preparation for a take **/
  protected synchronized void insert(Object x) { 
    //    if (item_ != null) throw new Error("Protocol error");
    item_ = x; 
  }

  /** Take item known to exist **/
  protected synchronized Object extract() { 
    //    if (item_ == null) throw new Error("Protocol error");
    Object x = item_;
    item_ = null;
    return x;
  }

  public synchronized Object peek() {
    return item_;
  }


  /**
   * Semaphore maintaining count of number of takes that
   * are waiting for puts.
   **/
  protected Semaphore unclaimedTakers_ = new WaiterPreferenceSemaphore(0);

  /** 
   * (binary) Semaphore that is released when an item has been
   * put, so a take may proceed.
   **/

  protected Semaphore itemAvailable_ = new WaiterPreferenceSemaphore(0);

  /**
   * (binary) Semaphore that is released when an item has been
   * taken, so a put may proceed. Since only one thread at a time can ever
   * be waiting on this, fairness is not important, so we use cheapest
   * implementation.
   **/

  protected Semaphore itemTaken_ = new Semaphore(0);


  // implementations of take and offer are too similar to bother separating

  public Object take() throws InterruptedException {
    return doTake(false, 0);
  }

  public Object poll(long msecs) throws InterruptedException {
    return doTake(true, msecs);
  }
  


  protected Object doTake(boolean timed, long msecs) throws InterruptedException {

    /*
      Basic protocol is:
      1. Announce that a taker has arrived (via unclaimedTakers_ semaphore), 
          so that a put can proceed.
      2. Wait until the item is put (via itemAvailable_ semaphore).
      3. Take the item, and signal the put (via itemTaken_ semaphore).

      Backouts due to interrupts or timeouts are allowed
      only during the wait for the item to be available. However,
      even here, if the put of an item we should get has already
      begun, we ignore the interrupt/timeout and proceed.

    */

    // Holds exceptions caught at times we cannot yet rethrow or reinterrupt
    InterruptedException interruption = null;

    // Records that a timeout or interrupt occurred while waiting for item
    boolean failed = false;

    // Announce that a taker is present
    unclaimedTakers_.release();

    // Wait for a put to insert an item.

    try {  
      if (!timed) 
        itemAvailable_.acquire();

      else if (!itemAvailable_.attempt(msecs)) 
        failed = true;
    }
    catch(InterruptedException ex) {
      interruption = ex;
      failed = true;
    }

    //  Messy failure mechanics

    if (failed) {

      // On interrupt or timeout, loop until either we can back out or acquire.
      // One of these must succeed, although it may take
      // multiple tries due to re-interrupts
      
      for (;;) { 

        // Contortion needed to avoid catching our own throw
        boolean backout = false; 

        try {
          // try to deny that we ever arrived.
          backout = unclaimedTakers_.attempt(0);
          
          // Cannot back out because a put is active. 
          // So retry the acquire.
          
          if (!backout && itemAvailable_.attempt(0))
            break;
        }
        catch (InterruptedException e) {
          if (interruption == null) // keep first one if a re-interrupt
            interruption = e;
        }
        
        if (backout) {
          if (interruption != null) 
            throw interruption;
          else // must have been timeout
            return null;
        }
      }
    }
    
    // At this point, there is surely an item waiting for us.
    // Take it, and signal the put


    Object x = extract();
    itemTaken_.release();

    // if we had to continue even though interrupted, reset status
    if (interruption != null) Thread.currentThread().interrupt();

    return x;

  }

  /**  Lock to ensure that only one put at a time proceeds **/

  protected Object onePut_ = new Object();

  public void put(Object x) throws InterruptedException {

    /*
      Basic protocol is:
      1. Wait until a taker arrives (via unclaimedTakers_ semaphore)
      2. Wait until no other puts are active (via onePut_)
      3. Insert the item, and signal taker that it is ready 
         (via itemAvailable_ semaphore).
      4. Wait for item to be taken (via itemTaken_ semaphore).
      5. Allow another put to insert item (by releasing onePut_).

      Backouts due to interrupts or (for offer) timeouts are allowed
      only during the wait for takers. Upon claiming a taker, puts
      are forced to proceed, ignoring interrupts.
    */

    unclaimedTakers_.acquire();
    doPut(x);
  }

  
  protected void doPut(Object x) {
    synchronized(onePut_) {
      insert(x);
      itemAvailable_.release();
      
      // Must ignore interrupts while waiting for acknowledgement
      boolean wasInterrupted = false;
      
      for (;;) {
        try {
          itemTaken_.acquire();
          break;
        }
        catch (InterruptedException ex) {
          wasInterrupted = true;
        }
      }

      if (wasInterrupted) Thread.currentThread().interrupt();
    }
  }

  public boolean offer(Object x, long msecs) throws InterruptedException {

    // Same as put, but with a timed wait for takers

    if (!unclaimedTakers_.attempt(msecs)) 
      return false;

    doPut(x);
    return true;
  }



}
