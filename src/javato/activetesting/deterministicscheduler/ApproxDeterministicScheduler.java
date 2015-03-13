package javato.activetesting.deterministicscheduler;


import java.util.concurrent.Semaphore;
import java.util.LinkedList;
import java.lang.reflect.Field;

import javato.activetesting.common.MersenneTwisterFast;
import javato.activetesting.common.WeakIdentityHashMap;
import javato.activetesting.common.Parameters;
import sun.misc.Unsafe;

/**
 * Copyright (c) 2006-2009,
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
public class ApproxDeterministicScheduler {
    private Thread currentRunningThread;
    final private WeakIdentityHashMap threadToSemaphoreMap = new WeakIdentityHashMap(89);
    final private WeakIdentityHashMap threadToTouchMap = new WeakIdentityHashMap(89);
    final private WeakIdentityHashMap threadToRandMap = new WeakIdentityHashMap(89);
    final public LinkedList<Pair> waitingThreadsFIFO = new LinkedList<Pair>();
    final private MersenneTwisterFast rand = new MersenneTwisterFast(Parameters.deterministicSchedulerRandomSeed);
    private boolean isActive = true;

    class Pair {
        public Thread thread;
        public Semaphore sem;

        Pair(Thread thread, Semaphore sem) {
            this.thread = thread;
            this.sem = sem;
        }

        public String toString() {
            return thread.toString();
        }
    }

    public ApproxDeterministicScheduler() {
        currentRunningThread = null;
        (new StallBreaker(this)).start(); 
    }

    public boolean isActive() {
        return isActive;
    }

    public Thread getCurrentRunningThread() {
        return currentRunningThread;
    }

    private Semaphore getSemaphore(Object o) {
        Object val = threadToSemaphoreMap.get(o);
        if (val == null) {
            val = new Semaphore(0);
            threadToSemaphoreMap.put(o, val);
        }
        return (Semaphore) val;
    }

    private MersenneTwisterFast getRand(Object o) {
        Object val = threadToRandMap.get(o);
        if (val == null) {
            val = new MersenneTwisterFast(rand.nextLong());
            threadToRandMap.put(o, val);
        }
        return (MersenneTwisterFast) val;
    }

    private void setTouched(Thread t) {
        threadToTouchMap.put(t, Boolean.TRUE);
    }

    public Boolean getAndUnsetTouched(Thread t) {
        Boolean ret = (Boolean)threadToTouchMap.get(t);
        threadToTouchMap.put(t, Boolean.FALSE);
        return ret;
    }

    private Semaphore prepareToWait(Thread currentThread) {
        Semaphore sem = getSemaphore(currentThread);
        waitingThreadsFIFO.addFirst(new Pair(currentThread,sem));
        return sem;
    }

    public synchronized void stopDeterministicScheduling() {
        isActive = false;
        while(!waitingThreadsFIFO.isEmpty()) {
            enableAWaitingThread();
        }
    }

    public void enableAWaitingThread() {
        if (!waitingThreadsFIFO.isEmpty()) {
            Pair p = waitingThreadsFIFO.removeLast();
            currentRunningThread = p.thread;
            p.sem.release();
        }
    }

    public void waitUntilChildHasPaused(Object child) {
        if (!isActive) return;
        Thread currentThread = Thread.currentThread();
        for (int i=0; i< 1000; i++) {
            try {
                synchronized (this) {
                    if (!isActive) return;
                    setTouched(currentThread);
                    for (Pair pair: waitingThreadsFIFO) {
                        if (pair.thread == child) return;
                    }
                }
                Thread.sleep(Parameters.afterStartSleepDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void schedulePoint(Integer iid, Object lock, boolean isLock) {
        Semaphore sem = null;
        if (!isActive) return;
        Thread currentThread = Thread.currentThread();
        boolean flag = false;
        long count = 0;
        int i = 0;
        do {
            synchronized (this) {
                if (!isActive) return;
                sem = null;
                i++;
                setTouched(currentThread);
                if (currentRunningThread == null) {
                    currentRunningThread = currentThread;
                }
                if (currentThread == currentRunningThread) {
                    if (!waitingThreadsFIFO.isEmpty()) {
                        if (!isEnabled(isLock,lock)) {
                            enableAWaitingThread();
                            sem = prepareToWait(currentThread);
                        } else {
                            float f = getRand(currentThread).nextFloat();
                            if (f < Parameters.deterministicSchedulerContextSwitchProbability) {
                                enableAWaitingThread();
                                sem = prepareToWait(currentThread);
                            }
                        }
                    }
                } else {
                    sem = prepareToWait(currentThread);
                }
            }
            if (sem!=null) {
                try { sem.acquire(); } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (this) {
               flag = (currentRunningThread!=currentThread) || !isEnabled(isLock,lock);
            }
        } while (i<10 && flag);
    }

    private boolean isEnabled(boolean isLock, Object lock) {
        if (isLock) {
            if (getUnsafe().tryMonitorEnter(lock)) {
                getUnsafe().monitorExit(lock);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static Unsafe getUnsafe() {
        Unsafe unsafe = null;

        try {
            Class uc = Unsafe.class;
            Field[] fields = uc.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals("theUnsafe")) {
                fields[i].setAccessible(true);
                unsafe = (Unsafe) fields[i].get(uc);
                break;
                }
            }
        }
        catch (Exception ignore) {}
        return unsafe;
    }
}
