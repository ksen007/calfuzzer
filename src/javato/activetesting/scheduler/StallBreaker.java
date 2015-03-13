package javato.activetesting.scheduler;

import javato.activetesting.activechecker.ActiveChecker;
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
public class StallBreaker extends Thread {
    public StallBreaker() {
        super("ipc1");
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
    }

    public void run() {
        Object lock = new Object();
        synchronized (lock) {
            try {
                while (true) {
                    lock.wait(Parameters.stallCheckerInterval);
                    breakAnyStall();
                }
            } catch (InterruptedException e) {
            }
        }

    }


    public static void breakAnyStall() {
        Thread[] tList = new Thread[Thread.activeCount()];
        int numThreads = Thread.enumerate(tList);
        int count = 0;
        int activeCount = 0;

        //System.out.println("----------------------------------------------------------");
        for (int i = 0; i < numThreads; i++) {
            //System.out.println("Thread " + tList[i] + " in state " + tList[i].getState()
            //        + " isDaemon " + tList[i].isDaemon() + " priority "+tList[i].getPriority());
            if (!tList[i].getName().equals("ipc1")
                    && !tList[i].getName().equals("ipc2")
                    && !tList[i].getName().equals("Keep-Alive-Timer")
                    && !tList[i].getName().equals("DestroyJavaVM")) {

                if (tList[i] != Thread.currentThread()
                        && (tList[i].getState() == Thread.State.RUNNABLE
                        || tList[i].getState() == Thread.State.TIMED_WAITING
                        || tList[i].getState() == Thread.State.NEW)) {
                    count++;
                }
                activeCount++;
            }
        }
        //System.out.println("Active Count "+activeCount+" count "+count);
        if (activeCount == 0) {
            System.exit(0);
        }
        if (count == 0) {
            if (!ActiveChecker.unblockAThread()) {
                //System.err.println("System stall identified by "+Thread.currentThread());
                //printThreadState();
                //System.exit(1);
            }
//            } else {
//                System.out.println("s");
//            }
        }
    }


    public static int getActiveThreadCount() {
        Thread[] tList = new Thread[Thread.activeCount()];
        int numThreads = Thread.enumerate(tList);
        int count = 0;

        for (int i = 0; i < numThreads; i++) {
            if (!tList[i].getName().equals("ipc1")
                    && !tList[i].getName().equals("ipc2")
                    && !tList[i].getName().equals("Keep-Alive-Timer")
                    && !tList[i].getName().equals("DestroyJavaVM")) {
                count++;
            }
        }
        return count;
    }

    public static void printThreadState() {
        Thread[] tList = new Thread[Thread.activeCount()];
        int numThreads = Thread.enumerate(tList);

        System.out.println("Printing Thread State: ");
        for (int i = 0; i < numThreads; i++) {
            System.out.println("Thread " + tList[i] + " in state " + tList[i].getState() + " isDaemon " + tList[i].isDaemon());
            StackTraceElement[] trace = tList[i].getStackTrace();
            for (StackTraceElement elem : trace) {
                System.out.println("    " + elem);
            }
        }

    }

}
