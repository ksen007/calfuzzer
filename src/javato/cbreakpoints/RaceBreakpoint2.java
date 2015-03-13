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
public class RaceBreakpoint2 extends CBreakpoints {
    private Object obj;
    private int id; // to distinguish various race breakpoints
	private boolean isFirst;

    final private static int thisId = Integer.getInteger("cbreakpoint.id",1);
    private boolean isSymmetric;

    public RaceBreakpoint2(int id,Object obj, boolean isFirst, boolean isSymmetric) {
        this.id = id;
        this.obj = obj;
        this.isFirst = isFirst;
        this.isSymmetric = isSymmetric;
    }

    public RaceBreakpoint2(Object obj, boolean isFirst, boolean isSymmetric) {
        this(1, obj, isFirst, isSymmetric);
    }

    public boolean predicateGlobal(CBreakpoints cb) {
        if ((cb instanceof RaceBreakpoint2) && obj == ((RaceBreakpoint2)cb).obj
			&& (isSymmetric || (isFirst != ((RaceBreakpoint2)cb).isFirst))){
            System.out.println("Race detected ");
            printStat(id + ":race");
            return true;
        }
        return false;
    }

    public boolean predicateLocal() {
        return id==thisId;
    }
}
