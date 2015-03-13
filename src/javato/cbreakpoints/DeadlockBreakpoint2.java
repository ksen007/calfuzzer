package javato.cbreakpoints;

import java.lang.management.*;

/**
 * Copyright (c) 2006-2010,
 * Chang-Seo Park <parkcs@cs.berkeley.edu>
 * Koushik Sen    <ksen@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class DeadlockBreakpoint2 extends CBreakpoints {
    private Object lock;
    private String hasLockType;
    private int id; // to distinguish various race breakpoints
	//private long thread;
	private MonitorInfo[] mi;
    final private static int thisId = Integer.getInteger("cbreakpoint.id",1);
	final private static boolean exitOnDeadlock = Boolean.getBoolean("cbreakpoint.exitOnDeadlock");
	private static boolean firstMatch = true;

    public DeadlockBreakpoint2(int id, Object lock, String hasLockType) {
        this.id = id;
        this.lock = lock;
		this.hasLockType = hasLockType;
		//this.thread = Thread.currentThread().getId();
    }

    public DeadlockBreakpoint2(int id, Object lock) {
		this(id, lock, null);
	}

    public DeadlockBreakpoint2(Object lock, String hasLockType) {
		this(1, lock, hasLockType);
	}

    public DeadlockBreakpoint2(Object lock) {
		this(1, lock);
	}

	public boolean predicateGlobal(CBreakpoints cb) {
		if (cb instanceof DeadlockBreakpoint2) {
			DeadlockBreakpoint2 that = (DeadlockBreakpoint2)cb;
			//System.out.println("predicateGlobal");
			//System.out.println("This thread has ");// + numLocksHeld());
			//printLocksHeld();
			//System.out.println("That thread has ");// + that.numLocksHeld());
			//that.printLocksHeld();
			//System.out.println("This lock " + printLock(lock) + " is " + (holdsLock(that.lock) ? "" : "not ") + "held by that thread"); 
			//System.out.println("That lock " + printLock(that.lock) + " is " + (that.holdsLock(that.lock) ? "" : "not ") + "held by this thread"); 
			if(that.holdsLock(lock) && holdsLock(that.lock)) {
				System.out.println("Deadlock detected ");
				if(firstMatch) {
					firstMatch = false;
					printStat(id + ":deadlock");
				}
				return true;
			}
		}
		return false;
	}

	public boolean predicateLocal() {
		/*if(numLocksHeld() > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("Trying to lock ");
			sb.append(lock.getClass());
			if(hasLockType != null) {
				sb.append(holdsLockType(hasLockType) ? ", holds lock: " : ", doesn't hold lock: ");
				sb.append(hasLockType);
			}
			System.out.println(sb);
		}*/
		return id==thisId && (numLocksHeld() > 0) 
			&& (hasLockType == null || holdsLockType(hasLockType));
	}

	public boolean breakHere(int millis) {
		/*
		System.out.println("DeadlockBreakpoint2::breakHere before locking: " + lock.getClass() + "@" + Integer.toHexString(lock.hashCode()) );
		ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] ti = tbean.getThreadInfo(new long[] {Thread.currentThread().getId()}, true, true);
		//System.out.println(ti);
		MonitorInfo[] mi = ti[0].getLockedMonitors();
		//System.out.println(mi.length);
		for(MonitorInfo m : mi) {
			System.out.println("Locked : " + m);
		}
		*/

		// Get locked monitors
		ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] ti = tbean.getThreadInfo(new long[] { Thread.currentThread().getId() }, true, true);
		mi = ti[0].getLockedMonitors();

		boolean ret = super.breakHere(true, millis);
		if(ret == true && exitOnDeadlock)
			Runtime.getRuntime().halt(127);

		return ret;
	}

	private boolean holdsLock(Object lock) {
		//ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
		//ThreadInfo[] ti = tbean.getThreadInfo(new long[] { thread }, true, true);
		//MonitorInfo[] mi = ti[0].getLockedMonitors();
		for(MonitorInfo m : mi) {
			//System.out.println(m.getClassName()+m.getIdentityHashCode());
			//System.out.println(lock.getClass().getName()+System.identityHashCode(lock));
			if(m.getClassName().equals(lock.getClass().getName()) && m.getIdentityHashCode()==System.identityHashCode(lock))
				return true;
		}
		return false;
	}

	private boolean holdsLockType(String type) {
		for(MonitorInfo m : mi) {
			if(m.getClassName().equals(type))
				return true;
		}
		return false;
	}

	private int numLocksHeld() {
		//ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
		//ThreadInfo[] ti = tbean.getThreadInfo(new long[] { thread }, true, true);
		//MonitorInfo[] mi = ti[0].getLockedMonitors();
		return mi.length;
	}

	private String printLock(Object lock) {
		return (lock.getClass() + "@" + Integer.toHexString(System.identityHashCode(lock)) );
	}

	private void printLocksHeld() {
		for(MonitorInfo m : mi) {
			System.out.println(m);
		}
	}
}
