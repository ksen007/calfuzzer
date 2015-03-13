package javato.activetesting;

import javato.activetesting.analysis.AnalysisImpl;
import javato.activetesting.analysis.ObserverForActiveTesting;
import javato.activetesting.analysis.DeterministicAnalysisImpl;
import javato.activetesting.hybridracedetection.CommutativePair;
import javato.activetesting.hybridracedetection.HybridRaceTracker;
import javato.activetesting.common.Parameters;
import javato.activetesting.common.MutableLong;
import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.threadrepro.EqualObjectBreakpoint;
import javato.activetesting.deterministicscheduler.StallBreaker;
import javato.activetesting.lockset.LockSetTracker;
import javato.activetesting.reentrant.IgnoreRentrantLock;
import javato.activetesting.igoodlock.Pair;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

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
public class PredictestAnalysis extends AnalysisImpl {
    private CommutativePair racePair;
    private Map<Integer, MutableLong> iidVisitCount = new TreeMap<Integer, MutableLong>();;
    volatile private int done = 0;
    volatile private boolean isLock = false;
    volatile private EqualObjectBreakpoint eq1 = null, eq2 = null;
    volatile private Thread thread1 = null, thread2 = null;
    volatile private int count1 = 0, count2 = 0;
    private Object memory1;
    private Integer iid1;
    private LockSetTracker lsTracker = new LockSetTracker();
    private IgnoreRentrantLock ignoreRentrantLock = new IgnoreRentrantLock();
    private boolean isDeadlock = false;


    private long incAndGetVisitCount(Integer iid) {
        if (Parameters.LOG_IID_VISIT_COUNT) {
            MutableLong l = iidVisitCount.get(iid);
            if (l==null) {
                l = new MutableLong(0);
                iidVisitCount.put(iid,l);
            }
            (l.val)++;
            return l.val;
        }
        return 0;
    }

    public void initialize() {
        if (Parameters.errorId >= 0) {
            LinkedHashSet<CommutativePair> seenRaces = HybridRaceTracker.getRacesFromFile();
            racePair = (CommutativePair) (seenRaces.toArray())[Parameters.errorId - 1];
            racePair.printcryptic(System.out);
        }
    }

    private void createRace(Integer iid, Object memory, boolean isLock) {
        if (done<2) {
            if (racePair != null && racePair.contains(iid)) {
                boolean isFirst = false;
                EqualObjectBreakpoint eq = null;
                synchronized (ActiveChecker.lock) {
                    if (isLock) this.isLock = true;
                    long visitCount = incAndGetVisitCount(iid);
                    if (done==0 && racePair.contains(iid,visitCount)) {
                        done++;
                        isFirst = Parameters.resolveOrder;
                        eq = eq1 = new EqualObjectBreakpoint(memory);
                        thread1 = Thread.currentThread();
                        memory1 = memory;
                        iid1 = iid;
//                        ((DeterministicAnalysisImpl)ObserverForActiveTesting.analysis).stopDeterministicScheduling();
                    } else if (done==1) {
                        if (!eq1.isAnyThreadWaiting() || (racePair.contains(iid1,iid) && memory1.equals(memory))) {
                            isFirst = !Parameters.resolveOrder;
                            eq = eq2 = new EqualObjectBreakpoint(memory);
                            thread2 = Thread.currentThread();
                            done++;
                            ((DeterministicAnalysisImpl)ObserverForActiveTesting.analysis).stopDeterministicScheduling();
                        }
                    }
                }

//                    if (racePair.contains(iid,visitCount)) {
//                        done++;
//                        isFirst = (done == 1);
//                        if (!Parameters.resolveOrder) isFirst = !isFirst;
//
//
//                        if (done==1) {
//                            eq = eq1 = new EqualObjectBreakpoint(memory);
//                            thread1 = Thread.currentThread();
//                        } else {
//                            eq = eq2 = new EqualObjectBreakpoint(memory);
//                            thread2 = Thread.currentThread();
//                            ((DeterministicAnalysisImpl)ObserverForActiveTesting.analysis).stopDeterministicScheduling();
//                        }
//                    }
//                }
                if (eq!=null) {
                    eq.begin(!isFirst);
                }
            }
        }

    }

    private void callEnd() {
        if (!isLock) {
            if (Thread.currentThread()==thread1) { thread1 = null; eq1.end();}
            if (Thread.currentThread()==thread2) { thread2 = null; eq2.end(); }
        }
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock, Object actualLock) {
        callEnd();
        if (isLock) {
            if (Thread.currentThread()==thread1) { count1++; }
            if (Thread.currentThread()==thread2) { count2++; }
        }
//        if (Parameters.trackLockRaces)
            createRace(iid,lock,true);
        synchronized (ActiveChecker.lock) {
            if (ignoreRentrantLock.lockBefore(thread, lock)) {
                boolean isDeadlock = lsTracker.lockBefore(iid, thread, lock);
                if (isDeadlock) {
                    Pair<Integer,Integer> piids = lsTracker.locationsInvolvedInDeadlock(thread,lock);
                    if (racePair.contains(piids.fst,piids.snd)) {
                        this.isDeadlock = true;
                    }
                }
            }
        }
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
        callEnd();
        if (isLock) {
            if (Thread.currentThread()==thread1) {
                if (count1 == 0) {thread1 = null; eq1.end(); }
                count1--;
            }
            if (Thread.currentThread()==thread2) {
                if (count2 == 0) {thread2 = null; eq2.end(); }
                count2--; 
            }
        }
        synchronized (ActiveChecker.lock) {
            if (ignoreRentrantLock.unlockAfter(thread, lock)) {
                lsTracker.unlockAfter(thread);
            }
        }
    }

    public void waitBefore(Integer iid, Integer thread, Integer lock) {
        // acquire lock
        callEnd();
        if (isLock) {
            if (Thread.currentThread()==thread1) { count1++; }
            if (Thread.currentThread()==thread2) { count2++; }
        }
        //if (Parameters.trackLockRaces)
            createRace(iid,lock,true);

        // release lock
        if (isLock) {
            if (Thread.currentThread()==thread1) {
                if (count1 == 0) {thread1 = null; eq1.end(); }
                count1--;
            }
            if (Thread.currentThread()==thread2) {
                if (count2 == 0) {thread2 = null; eq2.end(); }
                count2--;
            }
        }
    }

    public void readBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        callEnd();
        createRace(iid,memory,false);
    }

    public void writeBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        callEnd();
        createRace(iid,memory,false);
    }

    public void finish() {
        writeStat(Parameters.ERROR_STAT_FILE);
    }

    public void writeStat(String file) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(file,true));
            racePair.println(pw);
            pw.println(Parameters.errorId+":"
                    + (EqualObjectBreakpoint.isMatch?(isLock?"lock contention:":"data race:"):":")
                    + ((isDeadlock&& StallBreaker.isDeadlock)?"deadlock:":(StallBreaker.isStall?"stall:":":")));
            if (EqualObjectBreakpoint.isMatch) {
                if (isLock)
                    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>Lock contention detected "
                            +((isDeadlock && StallBreaker.isDeadlock)?" leading to a deadlock":""));
                else
                    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>Data race detected");
            }
            pw.close();
        } catch (IOException e) {
            System.err.println("Error while writing to " + file);
            System.exit(1);
        }

    }

}
