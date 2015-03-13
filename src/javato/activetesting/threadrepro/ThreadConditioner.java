package javato.activetesting.threadrepro;

import javato.activetesting.common.Parameters;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.LinkedList;
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
public abstract class ThreadConditioner {
    final static List<ThreadConditioner> pausedThreads = new LinkedList<ThreadConditioner>();


    private boolean ignoreWaitForTick = false;
    Sequencer seq = null;
    public static boolean isMatch = false;


    int pendingTickValue;
    int lastTickValue;
    Semaphore mySem;

    void addPendingTicksAndWaitsToSequencer(Sequencer seq) {
        this.seq = seq;
        if (mySem != null) {
            seq.put(pendingTickValue,mySem);
        }
    }

    abstract public boolean checkForMatchingState(boolean isFirst, boolean useConcreteTickValue);


    public boolean isAnyThreadWaiting() {
        synchronized (pausedThreads) {
            return !pausedThreads.isEmpty();
        }
    }

    public void setIgnoreWaitForTick(boolean ignoreWaitForTick) {
        this.ignoreWaitForTick = ignoreWaitForTick;
    }



    public void tick() {
        if (!ignoreWaitForTick) {
            synchronized (pausedThreads) {
                if (seq!=null) {
                    seq.tick();
                }
            }
        }
    }

    public void waitForTick(int tickValue, int timeoutInMS) {
        waitForTick(tickValue,false,timeoutInMS);
    }

    public void waitForTick(boolean isFirst, int timeoutInMS) {
        waitForTick(-1,isFirst,timeoutInMS);
    }

    private void waitForTick(int tickValue, boolean isFirst, int timeoutInMS) {
        Semaphore tmp = new Semaphore(0);
        if (!ignoreWaitForTick) {
            synchronized (pausedThreads) {
                if (seq==null) {
                    if (tickValue == -1)
                        isMatch = checkForMatchingState(isFirst,false) || isMatch;
                    else
                        isMatch = checkForMatchingState(false,true) || isMatch;
                }
                if (seq==null) {
                    pendingTickValue = tickValue;
                    mySem = tmp;
                } else {
                    if (tickValue == -1) {
                        lastTickValue += 2;
                        seq.put(lastTickValue,tmp);
                    } else {
                        seq.put(tickValue,tmp);

                    }
                }
            }
            try {
                if (timeoutInMS==0) {
                    tmp.acquire();
                } else if (!tmp.tryAcquire(timeoutInMS, TimeUnit.MILLISECONDS)) {
                    synchronized (pausedThreads) {
                        ListIterator<ThreadConditioner> iter = pausedThreads.listIterator();
                        while(iter.hasNext()) {
                            if (iter.next()==this) {
                                iter.remove();
                            }
                        }
                    }
                    System.err.println(Thread.currentThread()+" Timeout!!!");
                    setIgnoreWaitForTick(true);
                }
            } catch (InterruptedException e) {
                System.err.println(Thread.currentThread()+" Interrupt caught by waitForTick!!!");
                e.printStackTrace();
            }
        }
    }

    public void waitForTick(int tickValue) {
        waitForTick(tickValue, Parameters.raceBreakpointWaittime);
    }

    public void waitForTick(boolean isFirst) {
        waitForTick(isFirst, Parameters.raceBreakpointWaittime);
    }
}
