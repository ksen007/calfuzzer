package javato.cbreakpoints;

import javato.activetesting.threadrepro.Sequencer;
import javato.activetesting.threadrepro.ThreadConditioner;
import javato.activetesting.threadrepro.SequencerImpl;
import javato.activetesting.common.Parameters;

import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import java.io.PrintWriter;
import java.io.FileWriter;

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
abstract public class CBreakpoints {
    final static List<CBreakpoints> pausedThreads = new LinkedList<CBreakpoints>();
    final static Random rand = new Random();
    private Semaphore mySem;
	//private boolean firstBreak = true;
	private String file = System.getProperty("cbreakpoint.stat", "cbp.stat");

    abstract public boolean predicateGlobal(CBreakpoints cb);
    abstract public boolean predicateLocal();

    private boolean checkForMatchingState() {
        ListIterator<CBreakpoints> iter = pausedThreads.listIterator();
        while (iter.hasNext()) {
            CBreakpoints tc = iter.next();
            if (this.predicateGlobal(tc)) {
                tc.mySem.release();
                iter.remove();
                return true;
            }
        }
        pausedThreads.add(this);
        return false;
    }

    private void mySleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            System.err.println(Thread.currentThread()+"This exception is benign: sleep interrupted inside breakHere");
            e.printStackTrace();
        }

    }

    private void removeMe() {
        synchronized (pausedThreads) {
            ListIterator<CBreakpoints> iter = pausedThreads.listIterator();
            while(iter.hasNext()) {
                if (iter.next()==this) {
                    iter.remove();
                }
            }
        }        
    }

    public boolean breakHere(boolean isFirst, int timeoutInMS) {
        if (predicateLocal()) {
            boolean isMatch;
            mySem = new Semaphore(0);
            synchronized (pausedThreads) {
                isMatch = checkForMatchingState();
            }
            if (isMatch) {
                if(!isFirst) {
                    mySleep();
                }
                return true;
            } else {
                try {
                    timeoutInMS += rand.nextInt(timeoutInMS);
                    if (timeoutInMS <= 0 || !mySem.tryAcquire(timeoutInMS, TimeUnit.MILLISECONDS)) {
                        removeMe();
                        return false;
                    } else if(!isFirst) {
                        mySleep();
                    }
                    return true;
                } catch (InterruptedException e) {
                    System.err.println(Thread.currentThread()+" Interrupt caught by breakHere!!!");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

	public void printStat(String s) {
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file, true));
			pw.println(s);
			pw.close();
		}
		catch (Exception e) {
			System.err.println("Error writing stats to " + file);
		}
	}
}
