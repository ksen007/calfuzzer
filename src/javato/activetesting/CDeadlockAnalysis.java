package javato.activetesting;

import javato.activetesting.analysis.AnalysisImpl;
import javato.activetesting.common.Parameters;
import javato.activetesting.lockset.LockSetTracker;
import javato.activetesting.reentrant.IgnoreRentrantLock;
import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.igoodlock.Node;
import javato.activetesting.igoodlock.DeadlockCycleInfo;
import javato.activetesting.igoodlock.Pair;

import java.util.List;
import java.util.LinkedList;
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
public class CDeadlockAnalysis extends AnalysisImpl {
    private LockSetTracker lsTracker;
    private IgnoreRentrantLock ignoreRentrantLock;
    private List<Node> deadlockingCycle;
    private boolean isDeadlock = false;
    private int done = 0;

    private void printCycle(PrintWriter out) {
        for(Node node:deadlockingCycle) {
            List<Integer> context = node.getContext();
            out.print("[");
            for (Integer c:context) {
                out.print(javato.activetesting.analysis.Observer.getIidToLine(c));
                out.print(":");
            }
            out.print("]");
        }
        out.println();            
    }

    public void initialize() {
        synchronized (ActiveChecker.lock) {
            lsTracker = new LockSetTracker();
            ignoreRentrantLock = new IgnoreRentrantLock();
            DeadlockCycleInfo cycles = DeadlockCycleInfo.read();
            deadlockingCycle = cycles.getCycles().get(Parameters.errorId - 1);
            System.out.println("cycle " + deadlockingCycle);
        }
    }

    private boolean needToPause(List<Integer> lockSet) {
        for (Node node : deadlockingCycle) {
            List<Integer> tupleLs = node.getContext();
            if (lockSet.equals(tupleLs)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesCycle(Integer iid1, Integer iid2) {
        Node node = deadlockingCycle.get(0);
        Integer iid12 = ((LinkedList <Integer>)node.getContext()).getLast();
        node = deadlockingCycle.get(1);
        Integer iid22 = ((LinkedList <Integer>)node.getContext()).getLast();
        return ((iid12.equals(iid1) && (iid22.equals(iid2))) || (iid12.equals(iid2) && (iid22.equals(iid1))));
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock, Object actualLock) {
        boolean flag = false;
        synchronized (ActiveChecker.lock) {
            if (ignoreRentrantLock.lockBefore(thread, lock)) {
                boolean isDeadlock = lsTracker.lockBefore(iid, thread, lock);
                if (isDeadlock) {
                    Pair<Integer,Integer> piids = lsTracker.locationsInvolvedInDeadlock(thread,lock);
                    if (matchesCycle(piids.fst,piids.snd)) {
                        this.isDeadlock = true;
						finish();
						Runtime.getRuntime().halt(1);
                    }
                }
                List<Integer> lockSet = lsTracker.getLockSetIids(thread);
                if (needToPause(lockSet)) {
                    System.out.println("Match found");
                    done=1;
                    flag = true;
                }
            }
        }
        if (flag) {
            try {
                Thread.sleep(Parameters.raceBreakpointWaittime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
            if (ignoreRentrantLock.unlockAfter(thread, lock)) {
                lsTracker.unlockAfter(thread);
            }
        }
    }

    public void finish() {
        writeStat(Parameters.ERROR_STAT_FILE);
    }

    public void writeStat(String file) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(file,true));
            printCycle(pw);
            pw.println((isDeadlock?"deadlock:":":")+Parameters.errorId);
            pw.close();
        } catch (IOException e) {
            System.err.println("Error while writing to " + file);
            System.exit(1);
        }

    }

}

