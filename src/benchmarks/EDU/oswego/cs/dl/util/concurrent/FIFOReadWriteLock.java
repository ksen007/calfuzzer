/*
  File: FIFOReadWriteLock.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  11Jun1998  dl               Create public version
*/

package benchmarks.EDU.oswego.cs.dl.util.concurrent;

/**
 * This class implements a policy for reader/writer locks in which
 * incoming readers and writers contend in generally fair manner
 * for entry. When one reader enters, all others may enter. When
 * the last reader exits a waiting writer may enter.  This
 * does not provide globally FIFO behavior since readers arriving
 * after a writer may join other readers (in the style of a
 * Reader's preference RW lock). But it does ensure
 * FIFO ordering across writers, so earlier writers will write
 * first, modulo the caveats discussed with FIFOSemaphore, which
 * is used for queuing.
 * <p>
 * [<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>] <p>
 *
 * @see FIFOSemaphore
**/

public class FIFOReadWriteLock implements ReadWriteLock {

  /** 
    * FIFO queue of threads waiting for access 
    * Also serves as the Writer lock
   **/
  protected final FIFOSemaphore active_ = new FIFOSemaphore(1);
  public Sync writeLock() { return active_; }

  /**
   * Control reader access to active semaphore
  **/
  protected final Sync readerSync_ = new ReaderSync();
  public Sync readLock() { return readerSync_; }

  class ReaderSync implements Sync {

    protected int readers_ = 0;

    protected Mutex oneWaiter_ = new Mutex();

    protected synchronized void incReaders() throws InterruptedException { 
      // if first reader, wait for access, otherwise just proceed
      if (readers_ == 0) active_.acquire();
      ++readers_;
    }

    protected synchronized boolean tryRead(long msecs) throws InterruptedException {
      boolean pass = (readers_ > 0 || active_.attempt(msecs));
      if (pass) ++readers_;
      return pass;
    }

    public void acquire() throws InterruptedException {
      oneWaiter_.acquire();  // block if another is waiting for access
      try     { incReaders(); }
      finally { oneWaiter_.release();  }
    }

    public synchronized  void release()  { 
      if (--readers_ == 0) active_.release();
    }

    public boolean attempt(long msecs) throws InterruptedException {
      long startTime = (msecs <= 0)? 0 : System.currentTimeMillis();
      if (!oneWaiter_.attempt(msecs)) return false;

      long timeLeft = (msecs <= 0)? 0 :
        msecs - (System.currentTimeMillis() - startTime);

      try { return tryRead(timeLeft); }
      finally { oneWaiter_.release(); }
    }

  }

}

