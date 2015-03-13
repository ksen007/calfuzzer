package javato.activetesting.analysis;

import javato.activetesting.deterministicscheduler.ApproxDeterministicScheduler;
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
public class DeterministicAnalysisImpl implements Analysis {
    private Analysis next;
    private ApproxDeterministicScheduler sched;

    public DeterministicAnalysisImpl(Analysis next) {
        this.next = next;
        sched = new ApproxDeterministicScheduler();
    }

    public void stopDeterministicScheduling() {
        sched.stopDeterministicScheduling();
    }

    public void initialize() {
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock, Object actualLock) {
        sched.schedulePoint(iid, actualLock,true);
        next.lockBefore(iid,thread,lock,actualLock);
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
        //sched.schedulePoint(iid, null,false);
        next.unlockAfter(iid,thread,lock);
    }

    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) {
        sched.schedulePoint(iid, null,false);
        next.newExprAfter(iid,object,objOnWhichMethodIsInvoked);
    }

    public void methodEnterBefore(Integer iid, Integer thread) {
        sched.schedulePoint(iid, null,false);
        next.methodEnterBefore(iid,thread);
    }

    public void methodExitAfter(Integer iid, Integer thread) {
        sched.schedulePoint(iid, null,false);
        next.methodExitAfter(iid,thread);
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
        sched.schedulePoint(iid, null,false);
        next.startBefore(iid,parent,child);
    }

    public void startAfter(Integer iid, Integer parent, Object child) {
        sched.waitUntilChildHasPaused(child);
        next.startAfter(iid,parent,child);
    }

    public void waitBefore(Integer iid, Integer thread, Integer lock) {
        next.waitBefore(iid,thread,lock);
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {
        sched.schedulePoint(iid, null,false);
        next.waitAfter(iid,thread,lock);
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
        sched.schedulePoint(iid, null,false);
        next.notifyBefore(iid,thread,lock);
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
        sched.schedulePoint(iid, null,false);
        next.notifyAllBefore(iid,thread,lock);
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
        sched.schedulePoint(iid, null,false);
        next.joinAfter(iid,parent,child);
    }

    public void readBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        sched.schedulePoint(iid, null,false);
        next.readBefore(iid,thread,memory, isVolatile);
    }

    public void writeBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        sched.schedulePoint(iid, null,false);
        next.writeBefore(iid,thread,memory, isVolatile);
    }

    public void writeAfter(Integer iid, Thread thread, String local, Object value, String type) {
        next.writeAfter(iid,thread,local,value,type);
    }

    public void openDeterministicBlock(Integer bid) {
        next.openDeterministicBlock(bid);
    }

    public void closeDeterministicBlock(Integer bid) {
        next.closeDeterministicBlock(bid);
    }

    public void requireDeterministic(Integer thread, Object invariant) {
        next.requireDeterministic(thread,invariant);
    }

    public void assertDeterministic(Integer thread, Object invariant) {
        next.assertDeterministic(thread,invariant);
    }

    public void finish() {
        next.finish();
    }
}
