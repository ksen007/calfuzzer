/**
 * Copyright (c) 2009,
 * Jacob Burnim <jburnim@cs.berkeley.edu>
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

package javato.activetesting;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.Map;
import java.util.Set;

import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.AnalysisImpl;
import javato.activetesting.determinism.StackFrameTracker;
import javato.activetesting.determinism.MemoryGraph;
import javato.activetesting.determinism.MemoryGraphCapturer;

public class DeterminismInferenceAnalysis extends AnalysisImpl {
    private StackFrameTracker stackTracker;

    public void initialize() {
        synchronized (ActiveChecker.lock) {
            stackTracker = new StackFrameTracker();
        }
    }

    public void methodEnterBefore(Integer iid, Integer thread) {
        stackTracker.push();
    }

    public void methodExitAfter(Integer iid, Integer thread) {
        stackTracker.pop();
    }

    public void writeAfter(Integer iid, Thread thread, String local, Object value, String type) {
        stackTracker.set(local, value);
    }

    public void openDeterministicBlock(Integer thread) {
        // Capture pre-state.
        MemoryGraphCapturer mgc = new MemoryGraphCapturer();
        Set<Map.Entry<String,Object>> locals = stackTracker.getLocals(1);
        MemoryGraph G = mgc.capture(locals);
        try {
            ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream("execs", true));
            oos.writeObject(G);
            oos.close();
        } catch (IOException e) {
            System.err.println("Failed to write captured state.");
            e.printStackTrace();
        }
    }

    public void closeDeterministicBlock(Integer thread) {
        // Capture post-state.
        MemoryGraphCapturer mgc = new MemoryGraphCapturer();
        Set<Map.Entry<String,Object>> locals = stackTracker.getLocals(1);
        MemoryGraph G = mgc.capture(locals);
        try {
            ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream("execs", true));
            oos.writeObject(G);
            oos.close();
        } catch (IOException e) {
            System.err.println("Failed to write captured state.");
            e.printStackTrace();
        }
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
        synchronized (ActiveChecker.lock) {
            // Capture possible pre-state.
            System.err.println("\n***");
            System.err.println("Starting a thread --");
            System.err.println("  " + iid + ": " + parent + " -> " + child);
            System.err.println("Stack trace --");
            Thread.dumpStack();
            System.err.println("Local variables --");
            for (Map.Entry<String,Object> e : stackTracker.getLocals()) {
                System.err.println("  " + e.getKey() + ": " + e.getValue());
            }
            System.err.println("***\n");
        }
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) { }

    public void lockBefore(Integer iid, Integer thread, Integer lock,Object actualLock) { }
    public void unlockAfter(Integer iid, Integer thread, Integer lock) { }
    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) { }
    public void waitAfter(Integer iid, Integer thread, Integer lock) { }
    public void notifyBefore(Integer iid, Integer thread, Integer lock) { }
    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) { }
    public void readBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) { }
    public void writeBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) { }
    public void requireDeterministic(Integer thread, Object invariant) { }
    public void assertDeterministic(Integer thread, Object invariant) { }
    public void finish() { }
}
