package javato.activetesting;

import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.AnalysisImpl;
import javato.activetesting.analysis.Observer;
import javato.activetesting.igoodlock.GoodlockDS;
import javato.activetesting.reentrant.IgnoreRentrantLock;
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
public class IGoodlockAnalysis extends AnalysisImpl {
    private GoodlockDS gl;
    private IgnoreRentrantLock ignoreRentrantLock;

    public void initialize() {
        synchronized (ActiveChecker.lock) {
            ignoreRentrantLock = new IgnoreRentrantLock();
            gl = new GoodlockDS();
        }
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock, Object actualLock) {
        synchronized (ActiveChecker.lock) {
            if (ignoreRentrantLock.lockBefore(thread, lock)) {
                gl.lock(iid, thread, lock);
            }
        }
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
            if (ignoreRentrantLock.unlockAfter(thread, lock)) {
                gl.unlock(iid, thread, lock);
            }
        }
    }

    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) {
    }

    public void methodEnterBefore(Integer iid, Integer thread) {
    }

    public void methodExitAfter(Integer iid, Integer thread) {
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
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
    }

    public void writeBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
    }

    public void finish() {
        synchronized (ActiveChecker.lock) {
            int nDeadlocks;
            nDeadlocks = gl.dumpDeadlocks();
            Observer.writeIntegerList(Parameters.ERROR_LIST_FILE, nDeadlocks);
        }
    }
}
