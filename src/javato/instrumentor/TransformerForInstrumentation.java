package javato.instrumentor;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.util.Chain;

import java.util.Iterator;
import java.util.Map;

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

public class TransformerForInstrumentation extends BodyTransformer {
    private static TransformerForInstrumentation instance = new TransformerForInstrumentation();
    private Visitor visitor;

    private TransformerForInstrumentation() {
    }


    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    public static TransformerForInstrumentation v() {
        return instance;
    }


    protected void internalTransform(Body body, String pn, Map map) {
        SootMethod thisMethod = body.getMethod();
        String cName = thisMethod.getDeclaringClass().getName();

        if (cName.startsWith("javato.") || cName.startsWith("java.awt.event.NativeLibLoader") || thisMethod.isAbstract() || thisMethod.isNative()) {
            return;
        }

        visitor.thisClass = thisMethod.getDeclaringClass();
        Chain units = body.getUnits();

        visitor.visitMethodBegin(thisMethod, units);
        Iterator stmtIt = units.snapshotIterator();
        while (stmtIt.hasNext()) {
            Stmt s = (Stmt) stmtIt.next();
            visitor.thisStmt = s;
            visitor.visitStmt(thisMethod, units, s);
        }
        visitor.visitMethodEnd(thisMethod, units);
        body.validate();
    }


}
