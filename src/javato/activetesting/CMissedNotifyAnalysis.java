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
public class CMissedNotifyAnalysis extends AnalysisImpl {
    private CommutativePair racePair;
    private int foundSoFar = 0;
    private int stopAt = Integer.getInteger("javato.activetesting.stopat",1);

    public void initialize() {
        if (Parameters.errorId >= 0) {
            LinkedHashSet<CommutativePair> seenRaces = HybridRaceTracker.getRacesFromFile();
            racePair = (CommutativePair) (seenRaces.toArray())[Parameters.errorId - 1];
            racePair.printcryptic(System.out);
        }
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock, Object actualLock) {
        boolean flag = false;
        synchronized (ActiveChecker.lock) {
            if (racePair.containsWrite(iid)) {
                foundSoFar++;
                if (foundSoFar == stopAt) {
                    flag = true;
                    System.out.println("Match found at "+javato.activetesting.analysis.Observer.getIidToLine(iid));
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

}
