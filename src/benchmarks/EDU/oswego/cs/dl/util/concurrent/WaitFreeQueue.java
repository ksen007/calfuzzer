/*
  File: WaitFreeQueue.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  16Jun1998  dl               Create public version
   5Aug1998  dl               replaced int counters with longs
*/

package benchmarks.EDU.oswego.cs.dl.util.concurrent;

/**
 * A wait-free linked list based queue implementation,
 * adapted from the algorithm described in
 * <a
 * href="http://www.cs.rochester.edu/u/michael/PODC96.html"> Simple,
 * Fast, and Practical Non-Blocking and Blocking Concurrent Queue
 * Algorithms</a> by Maged M. Michael and Michael L. Scott.
 * This implementation is not strictly wait-free since it
 * relies on locking for basic atomicity and visibility requirements.
 * Locks can impose unbounded waits, although this should not 
 * be a major practical concern here since each lock is held
 * for the duration of only a few statements. (However, the
 * overhead of using so many locks can make it less attractive
 * than other Channel implementations on JVMs where locking
 * operations are very slow.)
 * <p>
 * The main advantage of this implementation over 
 * LinkedQueue
 * is that it does not strictly prohibit multiple concurrent
 * puts and/or multiple concurrent takes, but instead retries
 * these actions upon detection of interference.
 * Performance depends in part on the locking and scheduling
 * policies of the Java VM.
 * On at least some VMs, this implementation tends to perform well in
 * producer/consumer applications in which the queue is
 * hardly ever empty for long periods, normally because both the producers
 * and consumers are constantly active, and especially so on
 * multiple-CPU machines. However, it is a poor choice for
 * applications in which there is so much activity that
 * internal contention-based retries predominate computation, or 
 * in which take() may be expected to have to wait
 * for items to appear. The blocking take() operation performs a busy-wait
 * spin loop, which can needlessly eat up CPU time, especially
 * on uniprocessors. It would be a better idea in this case to
 * use an otherwise similar (and usually at least as efficient) 
 * LinkedQueue or BoundedLinkedQueue.
 * @see BoundedLinkedQueue
 * @see LinkedQueue
 * 
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]

 **/

public class WaitFreeQueue implements Channel {

  /**
   * Node class for linked list.
   * <p>
   * This is a faithful translation of Michael/Scott version. But
   * there are two accommodations to Java that
   * lead to a certain amount of ugliness. (Well,
   * a lot of ugliness.) 
   * <ul>
   *   <li> Since there is no atomic double-word read, compare,
   *    or compare-and-swap in java, all of these are
   *    simulated by synchronized blocks and methods.
   *
   *   <li> Their (ptr, count) `pointer' fields are expanded out
   *    into the Node class since operations should be synched
   *    on each Node anyway.
   * </ul>
   **/

  protected final static class Node { // list nodes
    protected Object  value; 
    protected Node    next = null;
    protected long     count = 0;     // version number of pointer
    /** Make a new node with indicated item, and null link **/
    protected Node(Object x) { value = x; }

    /** atomic equality test of versioned pointer **/
    protected final synchronized boolean pointerEquals(Node assumedNext, 
                                                       long  assumedCount) {
      return assumedNext == next && assumedCount == count;
    }
    
    /** simulated double-compare-and-swap **/
    protected final synchronized boolean commit(Node assumedNext, 
                                                long assumedCount,
                                                Node newNext,
                                                long newCount) {
      boolean success = (next == assumedNext && count == assumedCount);
      if (success) {  next = newNext; count = newCount; }
      return success;
    }

  }

  /**
   * head_ and tail_ are used only as counted pointers,
   * not as nodes. They intially both point to a dummy empty node.
   **/
  protected final Node head_;
  protected final Node tail_;

  public WaitFreeQueue() {
    Node dummy = new Node(null);
    head_ = new Node(null);
    tail_ = new Node(null);
    head_.next = dummy;
    tail_.next = dummy;
  }

  protected void insert(Object x) throws InterruptedException {
    Node node = new Node(x);

    for (;;) {

      if (Thread.interrupted()) throw new InterruptedException();

      // Atomically read tail
      Node tailDotNext;
      long tailDotCount;
      synchronized(tail_) { 
        tailDotNext = tail_.next; 
        tailDotCount = tail_.count;  
      }

      // Atomically read last (tail_.next)
      Node lastDotNext;
      long lastDotCount;
      synchronized(tailDotNext) { 
        lastDotNext = tailDotNext.next; 
        lastDotCount = tailDotNext.count;  
      }

      // only proceed if tail unchanged since read last
      if (tail_.pointerEquals(tailDotNext, tailDotCount)) { 

        if (lastDotNext == null) { // a spot is available to insert node
          if (tailDotNext.commit(lastDotNext, lastDotCount, 
                                 node, lastDotCount+1)) {

            tail_.commit(tailDotNext, tailDotCount, 
                         node, tailDotCount+1);
            return;
          }

        }
        else { // help out and retry
          tail_.commit(tailDotNext, tailDotCount, 
                       lastDotNext, tailDotCount+1);
        }
      }
    }
  }

  protected Object extract() throws InterruptedException {  
    for (;;) {

      if (Thread.interrupted()) throw new InterruptedException();

      // atomically read head, tail
      Node headDotNext;
      long headDotCount;
      synchronized(head_) { 
        headDotNext = head_.next; 
        headDotCount = head_.count;  
      }

      Node tailDotNext;
      long tailDotCount;
      synchronized(tail_) { 
        tailDotNext = tail_.next; 
        tailDotCount = tail_.count; 
      }

      Node first = headDotNext.next;

      // only proceed if head still same after reading tail
      if (head_.pointerEquals(headDotNext, headDotCount)) {

        if (headDotNext == tailDotNext) { 
          if (first == null) { // empty
            return null;
          }
          else {              // being updated
            tail_.commit(tailDotNext, tailDotCount, 
                         first, tailDotCount+1);
          }
        }
        else {                // valid
          Object x = first.value;
          if (head_.commit(headDotNext, headDotCount, 
                           first, headDotCount+1)) {
            first.value = null;
            return x;
          }
        }
      }
    }
  }

  /**
   * Spin until poll returns a non-null value.
   * A Thread.sleep(0) is performed on each iteration
   * as a heuristic to reduce contention. If you would
   * rather use, for example, an exponential backoff, 
   * you could manually set this up using poll. 
   **/
  public Object take() throws InterruptedException {

    for(;;) {
      Object x = extract();
      if (x != null)
        return x;
      else
        Thread.sleep(0);
    }
  }

  /**
   * Spin until poll returns a non-null value or time elapses.
   * if msecs is positive, a Thread.sleep(0) is performed on each iteration
   * as a heuristic to reduce contention.
   **/
  public Object poll(long msecs) throws InterruptedException {
    Object x = extract();
    if (x != null || msecs <= 0)
      return x;
    else {
      long startTime = System.currentTimeMillis();
      Thread.sleep(0);
      for(;;) {
        x = extract();
        if (x != null)
          return x;
        else if (System.currentTimeMillis() - startTime >= msecs)
          return null;
        else
          Thread.sleep(0);
      }
    }
  }

  public void put(Object x)  throws InterruptedException {
    if (x == null) throw new IllegalArgumentException();
    insert(x); 
  }

  public boolean offer(Object x, long msecs) throws InterruptedException { 
    if (x == null) throw new IllegalArgumentException();
    insert(x);
    return true;
  }


  public Object peek() {  

    // a simplified version of extract; still needs retries in case of updates

    for (;;) {

      if (Thread.interrupted()) return null;

      // atomically read head, tail
      Node headDotNext;
      long headDotCount;
      synchronized(head_) { 
        headDotNext = head_.next; 
        headDotCount = head_.count;  
      }

      Node tailDotNext;
      long tailDotCount;
      synchronized(tail_) { 
        tailDotNext = tail_.next; 
        tailDotCount = tail_.count; 
      }

      Node first = headDotNext.next;

      // only proceed if head still same after reading tail
      if (head_.pointerEquals(headDotNext, headDotCount)) {

        if (headDotNext == tailDotNext) { 
          if (first == null) { // empty
            return null;
          }
        }
        else {                // valid
          return first.value;
        }
      }
    }
  }

}


