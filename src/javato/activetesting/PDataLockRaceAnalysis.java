package javato.activetesting;

import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.AnalysisImpl;
import javato.activetesting.hybridracedetection.HybridRaceTracker;
import javato.activetesting.lockset.LockSet;
import javato.activetesting.lockset.LockSetTracker;
import javato.activetesting.reentrant.IgnoreRentrantLock;
import javato.activetesting.vc.VectorClockTracker;
import javato.activetesting.common.Parameters;

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
public class PDataLockRaceAnalysis extends AnalysisImpl {
    private VectorClockTracker vcTracker;
    private LockSetTracker lsTracker;
    private IgnoreRentrantLock ignoreRentrantLock;
    private HybridRaceTracker eb;

    public void initialize() {
        synchronized (ActiveChecker.lock) {
            vcTracker = new VectorClockTracker();
            lsTracker = new LockSetTracker();
            ignoreRentrantLock = new IgnoreRentrantLock();
            eb = new HybridRaceTracker();
        }
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock, Object actualLock) {
        synchronized (ActiveChecker.lock) {
            if (ignoreRentrantLock.lockBefore(thread, lock)) {
                if (Parameters.trackLockRaces) {
                    LockSet ls = lsTracker.getLockSet(thread);
                    Long mem = (long) lock;
                    eb.checkRace(iid, thread, mem , false, vcTracker.getVectorClock(thread), ls,true,false);
                    eb.addEvent(iid, thread, mem, false, vcTracker.getVectorClock(thread), ls);
                }
                boolean isDeadlock = lsTracker.lockBefore(iid, thread, lock);
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

    public void startBefore(Integer iid, Integer parent, Integer child) {
        synchronized (ActiveChecker.lock) {
            vcTracker.startBefore(parent, child);
        }
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {
        if (!Parameters.trackLockRaces) {
            synchronized (ActiveChecker.lock) {
                vcTracker.waitAfter(thread, lock);
            }
        }
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
        if (!Parameters.trackLockRaces) {
            synchronized (ActiveChecker.lock) {
                vcTracker.notifyBefore(thread, lock);
            }
        }
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
        if (!Parameters.trackLockRaces) {
            synchronized (ActiveChecker.lock) {
                vcTracker.notifyBefore(thread, lock);
            }
        }
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
        synchronized (ActiveChecker.lock) {
            vcTracker.joinAfter(parent, child);
        }
    }

    public void readBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        synchronized (ActiveChecker.lock) {
            LockSet ls = lsTracker.getLockSet(thread);
            eb.checkRace(iid, thread, memory, true, vcTracker.getVectorClock(thread), ls, false,isVolatile);
            eb.addEvent(iid, thread, memory, true, vcTracker.getVectorClock(thread), ls);
        }
    }

    public void writeBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        synchronized (ActiveChecker.lock) {
            LockSet ls = lsTracker.getLockSet(thread);
            eb.checkRace(iid, thread, memory, false, vcTracker.getVectorClock(thread), ls, false,isVolatile);
            eb.addEvent(iid, thread, memory, false, vcTracker.getVectorClock(thread), ls);
        }
    }

    public void finish() {
        synchronized (ActiveChecker.lock) {
            eb.dumpRaces();
        }
    }
}
