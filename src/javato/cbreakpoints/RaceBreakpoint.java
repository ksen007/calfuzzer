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
public class RaceBreakpoint extends CBreakpoints {
    private Object obj;
    private int id; // to distinguish various race breakpoints
	private boolean write; // access is read or write

    final private static int thisId = Integer.getInteger("cbreakpoint.id",1);
    private static boolean ignoreAfterFirst = Boolean.getBoolean("cbreakpoint.once");
	private static boolean firstMatch = true;

    public RaceBreakpoint(int id,Object obj, boolean write) {
        this.id = id;
        this.obj = obj;
		this.write = write;
    }

    public RaceBreakpoint(Object obj, boolean write) {
        this(1, obj, write);
    }

    public boolean predicateGlobal(CBreakpoints cb) {
        if ((cb instanceof RaceBreakpoint) && obj == ((RaceBreakpoint)cb).obj
			&& (write || ((RaceBreakpoint)cb).write)){
            System.out.println("Race detected ");
			if(firstMatch) {
				firstMatch = false;
				printStat(id + ":race");
			}
            return true;
        }
        return false;
    }

    public boolean predicateLocal() {
        return (!ignoreAfterFirst || firstMatch) && id==thisId;
    }
}
