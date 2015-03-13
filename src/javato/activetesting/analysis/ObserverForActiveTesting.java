package javato.activetesting.analysis;

import javato.activetesting.common.Parameters;
import javato.activetesting.common.IIDAccessCounter;

import java.util.LinkedList;

/**
 * Copyright (c) 2007-2008,
 * Pallavi Joshi	<pallavi@cs.berkeley.edu>
 * Koushik Sen  <ksen@cs.berkeley.edu>
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
public class ObserverForActiveTesting extends Observer {
    private static SyncMethodCache cache = new SyncMethodCache();
    public static Analysis analysis;
    //private static AtomicLong counter = new AtomicLong(0);
    //private static boolean stopRW = false;
    private static IIDAccessCounter counters = new IIDAccessCounter();

    static {
        System.out.println("Analysis class " + Parameters.analysisClass);
        if (Parameters.analysisClass != null) {
            try {
                Class t = Class.forName(Parameters.analysisClass);
                analysis = (Analysis) t.newInstance();
                if (Parameters.isDeterministicSchedule) {
                    analysis = new DeterministicAnalysisImpl(analysis);
                }

            } catch (Exception e) {
                System.err.println("Cannot find or instantiate Analysis class: " + Parameters.analysisClass + Thread.currentThread());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }


    public static java.lang.ThreadLocal lockStack = new java.lang.ThreadLocal() {
        protected synchronized Object initialValue() {
            return new LinkedList();
        }
    };

    public static java.lang.ThreadLocal iidStack = new java.lang.ThreadLocal() {
        protected synchronized Object initialValue() {
            return new LinkedList<Integer>();
        }
    };

    public static void myMethodEnterBefore(int iid) {
        analysis.methodEnterBefore(iid, uniqueId(Thread.currentThread()));
    }

    public static void myMethodExitAfter(int iid) {
        analysis.methodExitAfter(iid, uniqueId(Thread.currentThread()));
    }


    public static void myLockBefore(int iid, Object lock, String sig) {
        boolean isSynchronized = cache.isSynchronized(iid, lock, sig);
        if (isSynchronized) {
            ((LinkedList) lockStack.get()).addFirst(lock);
            analysis.lockBefore(iid, uniqueId(Thread.currentThread()), uniqueId(lock),lock);
        } else {
            ((LinkedList) lockStack.get()).addFirst(null);
        }
        ((LinkedList<Integer>) iidStack.get()).addFirst(iid);
    }

    public static void myLockBefore(int iid, int oid, String className) {
        Class c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        analysis.lockBefore(iid, uniqueId(Thread.currentThread()), oid, c);
    }

    public static void myLockBefore(int iid, Object lock) {
        analysis.lockBefore(iid, uniqueId(Thread.currentThread()), uniqueId(lock),lock);
    }

    public static void myUnlockAfter(int iid) {
        LinkedList ls = ((LinkedList) lockStack.get());
        LinkedList<Integer> is = ((LinkedList<Integer>) iidStack.get());
        Object lock = ls.removeFirst();
        int entryIid = is.removeFirst();
        while (iid != entryIid + 1) { // this is a hack; needs better handling in future
            if (lock != null) {
                analysis.unlockAfter(iid, uniqueId(Thread.currentThread()), uniqueId(lock));
            }
            lock = ls.removeFirst();
            entryIid = is.removeFirst();
        }
        if (iid != entryIid + 1) {
            System.out.println("thread " + uniqueId(Thread.currentThread()));
        }
        assert iid == entryIid + 1;
        if (lock != null) {
            analysis.unlockAfter(iid, uniqueId(Thread.currentThread()), uniqueId(lock));
        }
    }

    public static void myUnlockAfter(int iid, int oid) {
        analysis.unlockAfter(iid, uniqueId(Thread.currentThread()), oid);
    }

    public static void myUnlockAfter(int iid, Object lock) {
        analysis.unlockAfter(iid, uniqueId(Thread.currentThread()), uniqueId(lock));
    }

    public static void myNewExprInANonStaticMethodAfter(int iid, Object o, Object objOnWhichMethodIsInvoked) {
        analysis.newExprAfter(iid, uniqueId(o), uniqueId(objOnWhichMethodIsInvoked));
    }

    public static void myNewExprInAStaticMethodAfter(int iid, Object o) {
        analysis.newExprAfter(iid, uniqueId(o), 0);
    }

    public static void myStartBefore(int iid, Object t) {
        analysis.startBefore(iid, uniqueId(Thread.currentThread()), uniqueId(t));
    }

    public static void myStartAfter(int iid, Object t) {
        analysis.startAfter(iid, uniqueId(Thread.currentThread()), t);
    }


    public static void myWaitBefore(int iid, Object lock) {
        analysis.waitBefore(iid, uniqueId(Thread.currentThread()), uniqueId(lock));
    }

    public static void myWaitAfter(int iid, Object lock) {
        analysis.waitAfter(iid, uniqueId(Thread.currentThread()), uniqueId(lock));
    }

    public static void myNotifyBefore(int iid, Object lock) {
        analysis.notifyBefore(iid, uniqueId(Thread.currentThread()), uniqueId(lock));
    }

    public static void myNotifyAllBefore(int iid, Object lock) {
        analysis.notifyAllBefore(iid, uniqueId(Thread.currentThread()), uniqueId(lock));
    }

    public static void myJoinAfter(int iid, Object thread) {
        analysis.joinAfter(iid, uniqueId(Thread.currentThread()), uniqueId(thread));
    }

    public static void myReadBefore(int iid, Object o, int field) {
        if (counters.needToIgnore(iid)) return;
        analysis.readBefore(iid, uniqueId(Thread.currentThread()), id(o, field), false);
    }

    public static void myReadBefore(int iid, int clss, int field) {
        if (counters.needToIgnore(iid)) return;
        analysis.readBefore(iid, uniqueId(Thread.currentThread()), idInt(clss, field), false);
    }

    public static void myVReadBefore(int iid, Object o, int field) {
        if (counters.needToIgnore(iid)) return;
        analysis.readBefore(iid, uniqueId(Thread.currentThread()), id(o, field), true);
    }

    public static void myVReadBefore(int iid, int clss, int field) {
        if (counters.needToIgnore(iid)) return;
        analysis.readBefore(iid, uniqueId(Thread.currentThread()), idInt(clss, field), true);
    }

    public static void myWriteBefore(int iid, Object o, int field) {
        if (counters.needToIgnore(iid)) return;
        analysis.writeBefore(iid, uniqueId(Thread.currentThread()), id(o, field), false);
    }

    public static void myWriteBefore(int iid, int clss, int field) {
        if (counters.needToIgnore(iid)) return;
        analysis.writeBefore(iid, uniqueId(Thread.currentThread()), idInt(clss, field), false);
    }

    public static void myVWriteBefore(int iid, Object o, int field) {
        if (counters.needToIgnore(iid)) return;
        analysis.writeBefore(iid, uniqueId(Thread.currentThread()), id(o, field), true);
    }

    public static void myVWriteBefore(int iid, int clss, int field) {
        if (counters.needToIgnore(iid)) return;
        analysis.writeBefore(iid, uniqueId(Thread.currentThread()), idInt(clss, field), true);
    }


    public static void myWriteAfter(int iid, String local, Object value, String type) {
        analysis.writeAfter(iid, Thread.currentThread(), local, value, type);
    }

    public static void myWriteAfter(int iid, String local, byte value) {
        analysis.writeAfter(iid, Thread.currentThread(), local, value, "java.lang.Byte");
    }

    public static void myWriteAfter(int iid, String local, char value) {
        analysis.writeAfter(iid, Thread.currentThread(), local, value, "java.lang.Character");
    }

    public static void myWriteAfter(int iid, String local, short value) {
        analysis.writeAfter(iid, Thread.currentThread(), local, value, "java.lang.Short");
    }

    public static void myWriteAfter(int iid, String local, int value) {
        analysis.writeAfter(iid, Thread.currentThread(), local, value, "java.lang.Integer");
    }

    public static void myWriteAfter(int iid, String local, long value) {
        analysis.writeAfter(iid, Thread.currentThread(), local, value, "java.lang.Long");
    }

    public static void myWriteAfter(int iid, String local, float value) {
        analysis.writeAfter(iid, Thread.currentThread(), local, value, "java.lang.Float");
    }

    public static void myWriteAfter(int iid, String local, double value) {
        analysis.writeAfter(iid, Thread.currentThread(), local, value, "java.lang.Double");
    }

    public static void myWriteAfter(int iid, String local, boolean value) {
        analysis.writeAfter(iid, Thread.currentThread(), local, value, "java.lang.Boolean");
    }

    public static void myOpenDeterministicBlock(int iid) {
        analysis.openDeterministicBlock(uniqueId(Thread.currentThread()));
    }

    public static void myCloseDeterministicBlock(int iid) {
        analysis.closeDeterministicBlock(uniqueId(Thread.currentThread()));
    }

    /** Parameter 'invariant' must be serializable. */
    public static void requireDeterministic(Object invariant) {
        analysis.requireDeterministic(uniqueId(Thread.currentThread()), invariant);
    }

    /** Parameter 'invariant' must be serializable. */
    public static void assertDeterministic(Object invariant) {
        analysis.assertDeterministic(uniqueId(Thread.currentThread()), invariant);
    }
}
