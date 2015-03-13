package javato.activetesting;

import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.CheckerAnalysisImpl;
import javato.activetesting.common.Parameters;
import javato.activetesting.hybridracedetection.CommutativePair;
import javato.activetesting.hybridracedetection.HybridRaceTracker;
import javato.activetesting.racefuzzer.RaceChecker;

import java.util.LinkedHashSet;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Copyright (c) 2007-2008,
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
public class RaceFuzzerAnalysis extends CheckerAnalysisImpl {
    private CommutativePair racePair;

    public void initialize() {
        if (Parameters.errorId >= 0) {
            LinkedHashSet<CommutativePair> seenRaces = HybridRaceTracker.getRacesFromFile();
            racePair = (CommutativePair) (seenRaces.toArray())[Parameters.errorId - 1];
        }
        //System.out.println("IID pairs "+racePair);
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock, Object actualLock) {
        if (Parameters.trackLockRaces) {
            if (racePair != null && racePair.contains(iid)) {
                synchronized (ActiveChecker.lock) {
                    (new RaceChecker((long)lock, true, iid, true)).check();
                }
                ActiveChecker.blockIfRequired();
            }
        }
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
    }

    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) {
    }

    public void methodEnterBefore(Integer iid, Integer thread) {
    }

    public void methodExitAfter(Integer iid, Integer thread) {
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
    }

    public void startAfter(Integer iid, Integer parent, Object child) {
        try {
            Thread.sleep(Parameters.afterStartSleepDuration);
        } catch (InterruptedException ex) {

        }
    }
    
    public void waitAfter(Integer iid, Integer thread, Integer lock) {
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
    }

    public void readBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        if (racePair != null && racePair.contains(iid)) {
            //System.out.println("read ...");
            synchronized (ActiveChecker.lock) {
                (new RaceChecker(memory, false, iid, false)).check();
            }
            ActiveChecker.blockIfRequired();
        }
    }

    public void writeBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        if (racePair != null && racePair.contains(iid)) {
            //System.out.println("write ...");
            synchronized (ActiveChecker.lock) {
                (new RaceChecker(memory, true, iid, false)).check();
            }
            ActiveChecker.blockIfRequired();
        }
    }

    public void finish() {
        writeStat(Parameters.ERROR_STAT_FILE);
    }

    public static void writeStat(String file) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(file,true));
            pw.print(Parameters.errorId+":"+RaceChecker.isRace+" ");
            pw.close();
        } catch (IOException e) {
            System.err.println("Error while writing to " + file);
            System.exit(1);
        }

    }

}
