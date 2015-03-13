/*
  File: ReentrantWriterPreferenceReadWriteLock.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  26aug1998  dl                 Create public version

*/

package benchmarks.EDU.oswego.cs.dl.util.concurrent;

/** 
 * A writer-preference ReadWriteLock that allows writers to reacquire
 * read or write locks in the style of a ReentrantLock.
 * Readers are not allowed until all write locks held by
 * the writing thread have been released.
 * Readers may also reacquire read locks, but not write locks.
 * Among other applications, reentrancy can be useful when
 * write locks are held during calls or callbacks to methods that perform
 * reads under read locks.
 * <p>
 * <b>Sample usage</b>. Here is a code sketch showing how to exploit
 * reentrancy to perform lock downgrading after updating a cache:
 * <pre>
 * class CachedData {
 *   Object data;
 *   volatile boolean cacheValid;
 *   ReentrantWriterPreferenceReadWriteLock rwl = ...
 *
 *   void processCachedData() {
 *     rwl.readLock().acquire();
 *     if (!cacheValid) {
 *
 *        // upgrade lock:
 *        rwl.readLock().release();   // must release first to obtain writelock
 *        rwl.writeLock().acquire();
 *        if (!cacheValid) { // recheck
 *          data = ...
 *          cacheValid = true;
 *        }
 *        // downgrade lock
 *        rwl.readLock().acquire();  // reacquire read without giving up lock
 *        rwl.writeLock().release(); // release write, still hold read
 *     }
 *
 *     use(data);
 *     rwl.readLock().release();
 *   }
 * }
 * </pre>
 *
 * 
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * @see ReentrantLock
 **/

public class ReentrantWriterPreferenceReadWriteLock extends WriterPreferenceReadWriteLock {

  /** Number of number of acquires on write lock by activeWriter_ thread **/
  protected long writeHolds_ = 0;  

  protected boolean allowReader() {
    return (activeWriter_ == null && waitingWriters_ == 0) ||
      activeWriter_ == Thread.currentThread();
  }

  protected synchronized boolean startWrite() {
    if (writeHolds_ == 0) {
      if (activeReaders_ == 0) {
        activeWriter_ = Thread.currentThread();
        writeHolds_ = 1;
        return true;
      }
      else
        return false;
    }
    else if (activeWriter_ == Thread.currentThread()) {
      ++writeHolds_;
      return true;
    }
    else
      return false;
  }


  protected synchronized Signaller endRead() {
    --activeReaders_;

    if (writeHolds_ > 0) // a write lock is still held by current thread
      return null;
    else if (activeReaders_ == 0 && waitingWriters_ > 0)
      return writerLock_;
    else
      return null;

  }

  protected synchronized Signaller endWrite() {
    --writeHolds_;
    if (writeHolds_ > 0)   // still being held
      return null;
    else {
      activeWriter_ = null;
      if (waitingReaders_ > 0 && allowReader())
        return readerLock_;
      else if (waitingWriters_ > 0)
        return writerLock_;
      else
        return null;
    }
  }

}

