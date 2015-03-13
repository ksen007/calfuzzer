package javato.cbreakpoints;

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
public class AtomicityBreakpoint extends CBreakpoints {
    private Object obj;
    private int id; // to distinguish various race breakpoints
	private boolean atomic; // if access is in atomic block

    final private static int thisId = Integer.getInteger("cbreakpoint.id",1);
	final private static int ignoreFirst = Integer.getInteger("cbreakpoint.ignoreFirst", 0);
	private static boolean firstMatch = true;
	//private static volatile boolean inAtomic = false;
	private static int breakHits = 0;
	

    public AtomicityBreakpoint(int id,Object obj, boolean atomic) {
        this.id = id;
        this.obj = obj;
		this.atomic = atomic;
    }

    public AtomicityBreakpoint(Object obj, boolean atomic) {
        this(1, obj, atomic);
    }

    public boolean predicateGlobal(CBreakpoints cb) {
        if ((cb instanceof AtomicityBreakpoint) && obj == ((AtomicityBreakpoint)cb).obj
			&& (atomic || ((AtomicityBreakpoint)cb).atomic)){
            System.out.println("Atomicity violation detected ");
			if(firstMatch) {
				firstMatch = false;
				printStat(id + ":atomviol");
			}
            return true;
        }
        return false;
    }

    public boolean predicateLocal() {
        return id==thisId;
        //return id==thisId && (atomic || inAtomic);
    }

	public boolean breakHere(boolean isFirst, int timeoutInMS) {
		++breakHits;
		if(breakHits % 100 == 0) {
			System.err.println( "Breakpoints hit: " + breakHits + (breakHits <= ignoreFirst ? "(ignoring)" : ""));
		}
		if(breakHits <= ignoreFirst)
			return false;

		/*if(atomic)
			inAtomic = true;
		else {
			if(!inAtomic)
				return false;
		}*/
		boolean ret = super.breakHere(isFirst, timeoutInMS);
		/*if(atomic)
			inAtomic = false;*/
		return ret;
	}
}
