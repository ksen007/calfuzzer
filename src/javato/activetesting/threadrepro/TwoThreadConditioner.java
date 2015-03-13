package javato.activetesting.threadrepro;

import java.util.ListIterator;

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
public abstract class TwoThreadConditioner extends ThreadConditioner {

    public abstract boolean check(ThreadConditioner tc);

    public boolean checkForMatchingState(boolean isFirst, boolean useConcreteTickValue) {
        ListIterator<ThreadConditioner> iter = pausedThreads.listIterator();
        while (iter.hasNext()) {
            ThreadConditioner tc = iter.next();
            if (this.check(tc)) {
                System.out.println("***************** "+Thread.currentThread()+" Match Found!");
                Sequencer seq = new SequencerImpl();
                if (!useConcreteTickValue) {
                    this.lastTickValue = isFirst?-1:0;
                    tc.pendingTickValue = isFirst?2:1;
                    tc.lastTickValue = isFirst?2:1;
                }
                addPendingTicksAndWaitsToSequencer(seq);
                iter.remove();
                tc.addPendingTicksAndWaitsToSequencer(seq);
                seq.tick();
                return true;
            }
        }
        pausedThreads.add(this);
        return false;
    }

    public void begin(boolean isFirst, int timeoutInMS) {
        waitForTick(isFirst,timeoutInMS);
    }

    public void begin(boolean isFirst) {
        waitForTick(isFirst);
    }


    public void end(int timeoutInMS) {
        tick();
        waitForTick(false,timeoutInMS);
        tick();
    }

    public void end() {
        tick();
        waitForTick(false);
        tick();
    }

}
