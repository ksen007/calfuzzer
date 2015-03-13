package javato.activetesting.hybridracedetection;

import javato.activetesting.analysis.Observer;

import java.io.Serializable;
import java.io.PrintWriter;
import java.io.PrintStream;

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
public class CommutativePair implements Serializable {
    private Integer x;
    private Long xCount;
    private boolean isXRead;
    private Integer y;
    private Long yCount;
    private boolean isYRead;
    private boolean isLock;
    private boolean isVolatile;


    public CommutativePair(Integer x, Long xCount, boolean XRead, Integer y, Long yCount,
                           boolean YRead, boolean lock, boolean aVolatile) {
        this.x = x;
        this.xCount = xCount;
        isXRead = XRead;
        this.y = y;
        this.yCount = yCount;
        isYRead = YRead;
        isLock = lock;
        isVolatile = aVolatile;
    }

    public boolean containsWrite(Integer iid) {
        return (x.equals(iid) && !isXRead) || (y.equals(iid) && !isYRead);
    }

    public boolean contains(Integer iid) {
        return x.equals(iid) || y.equals(iid);
    }

    public boolean contains(Integer iid1, Integer iid2) {
        return (x.equals(iid1) && y.equals(iid2)) || (y.equals(iid1) && x.equals(iid2));
    }

    public int hashCode() {
        return x + y;
    }


    public boolean equals(Object object) {
        if (object == null) return false;
        if (!(object instanceof CommutativePair)) return false;
        CommutativePair c = (CommutativePair) object;
        return (x.equals(c.x) && y.equals(c.y)) || (x.equals(c.y) && y.equals(c.x));
    }

    public String toString() {
        return x + " " + y;
    }

    public void println(PrintWriter out) {
        out.println((isVolatile?"volatile ":"")
                +(isLock?"lock@":(isXRead?"read@":"write@"))
                +javato.activetesting.analysis.Observer.getIidToLine(x)
                +"||"+(isVolatile?"volatile ":"")
                +(isLock?"lock@":(isYRead?"read@":"write@"))
                +javato.activetesting.analysis.Observer.getIidToLine(y));
    }

    public void printcryptic(PrintStream out) {
        out.println(Observer.getIidToLine(x)+":"+xCount+":"+isXRead+":"
                +Observer.getIidToLine(y)+":"+yCount+":"+isYRead+":"+isLock+":"+isVolatile);
    }

    public boolean contains(Integer iid, long visitCount) {
        if ((x.equals(iid) && xCount==visitCount) || (y.equals(iid) && yCount==visitCount))
            return true;
        else
            return false;
    }
}
