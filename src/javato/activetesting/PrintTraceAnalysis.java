package javato.activetesting;

import javato.activetesting.analysis.AnalysisImpl;
import javato.activetesting.activechecker.ActiveChecker;

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
public class PrintTraceAnalysis extends AnalysisImpl {
    public void initialize() {
        synchronized (ActiveChecker.lock) {
            System.out.println("initialize()");
        }
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock, Object actualLock) {
        synchronized (ActiveChecker.lock) {
            System.out.println("lockBefore("+iid+","+thread+","+lock+")");
        }
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
            System.out.println("unlockAfter("+iid+","+thread+","+lock+")");
        }
    }

    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) {
        synchronized (ActiveChecker.lock) {
            System.out.println("newExprAfter("+iid+","+object+","+objOnWhichMethodIsInvoked+")");
        }
    }

    public void methodEnterBefore(Integer iid, Integer thread) {
        synchronized (ActiveChecker.lock) {
            System.out.println("methodEnterBefore("+iid+")");
        }
    }

    public void methodExitAfter(Integer iid, Integer thread) {
        synchronized (ActiveChecker.lock) {
            System.out.println("methodExitAfter("+iid+")");
        }
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
        synchronized (ActiveChecker.lock) {
            System.out.println("startBefore("+iid+","+parent+","+child+")");
        }
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
            System.out.println("waitAfter("+iid+","+thread+","+lock+")");
        }
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
            System.out.println("notifyBefore("+iid+","+thread+","+lock+")");
        }
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
            System.out.println("notifyAllBefore("+iid+","+thread+","+lock+")");
        }
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
        synchronized (ActiveChecker.lock) {
            System.out.println("joinAfter("+iid+","+parent+","+child+")");
        }
    }

    public void readBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        synchronized (ActiveChecker.lock) {
            System.out.println("readBefore("+iid+","+thread+","+memory+")");
        }
    }

    public void writeBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        synchronized (ActiveChecker.lock) {
            System.out.println("writeBefore("+iid+","+thread+","+memory+")");
        }
    }

    public void finish() {
        synchronized (ActiveChecker.lock) {
            System.out.println("finish()");
        }
    }
}
