package javato.activetesting.deterministicscheduler;

import javato.activetesting.common.Parameters;
import javato.activetesting.activechecker.ActiveChecker;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
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
public class StallBreaker extends Thread {
    final private ApproxDeterministicScheduler sched;
    public static boolean isDeadlock = false;
    public static boolean isStall = false;
    private Thread oldThread;

    public StallBreaker(ApproxDeterministicScheduler sched) {
        super("sb1");
        this.sched = sched;
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
    }

    public void run() {
        int i = 0;
        try {
            while (true) {
                i++;
                Thread.sleep(Parameters.thrilleStallCheckerInterval);
                if (i==50) {
                    i = 0;
                    ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
                    long [] tids = tbean.findDeadlockedThreads();
                    if (tids != null) {
                        isDeadlock = true;
                        System.err.println("-------------------------- Deadlock found ------------------");
                        for (int j=0; j < tids.length;j++) {
                            System.err.println("Thread "+tbean.getThreadInfo(tids[j]));
                        }
                        printThreadState(true);
                        System.exit(1);
                    }
                    //breakAnyStall();
                }
                synchronized (sched) {
                    if (sched.isActive()) {
                        Thread t = sched.getCurrentRunningThread();
                        if (t!=null && t == oldThread) {
                            if (!sched.getAndUnsetTouched(t)) {
                                //System.out.println("&&&&&&&&&&&&&&&& bad "+t+sched.waitingThreadsFIFO);
                                sched.enableAWaitingThread();
                            }
                        }
                        oldThread = t;
                    }
                }
            }
        } catch (InterruptedException e) {
        }
    }

    public static void breakAnyStall() {
        Thread[] tList = new Thread[Thread.activeCount()];
        int numThreads = Thread.enumerate(tList);
        int count = 0;
        int activeCount = 0;

        for (int i = 0; i < numThreads; i++) {
            if (!tList[i].getName().equals("sb1")
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
        if (count == 0) {
            System.err.println("******************************System Stall******************************");
            isStall = true;
            printThreadState(false);
            System.exit(1);
        }
    }

    public static void printThreadState(boolean isDeadlock) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(Parameters.ERROR_STALL_FILE,true));

            Thread[] tList = new Thread[Thread.activeCount()];
            int numThreads = Thread.enumerate(tList);
            pw.println("-----------------------------------------------------------------------------------------");
            pw.println("Printing Thread State for "+(!isDeadlock?"(communication deadlock)":"(resource deadlock)"));
            for (int i = 0; i < numThreads; i++) {
                if (!tList[i].getName().equals("sb1")
                        && !tList[i].getName().equals("Keep-Alive-Timer")
                        && !tList[i].getName().equals("DestroyJavaVM")) {
                    pw.println("Thread " + tList[i] + " in state " + tList[i].getState() + " isDaemon " + tList[i].isDaemon());
                    StackTraceElement[] trace = tList[i].getStackTrace();
                    pw.println("    " + trace[trace.length-1]);
                }
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
