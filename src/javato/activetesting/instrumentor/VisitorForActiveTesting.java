package javato.activetesting.instrumentor;

import java.util.LinkedList;

import javato.instrumentor.UnknownASTNodeException;
import javato.instrumentor.Visitor;
import javato.instrumentor.contexts.*;
import javato.activetesting.common.Parameters;
import soot.*;
import soot.jimple.*;
import soot.util.Chain;

/**
 * Copyright (c) 2007-2008
 * Pallavi Joshi	<pallavi@cs.berkeley.edu>
 * Koushik Sen <ksen@cs.berkeley.edu>
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
public class VisitorForActiveTesting extends Visitor {

    public static final String openDeterministicBlockSig
        = "<edu.berkeley.cs.detcheck.Determinism: void openDeterministicBlock()>";

    public static final String closeDeterministicBlockSig
        = "<edu.berkeley.cs.detcheck.Determinism: void closeDeterministicBlock()>";

    // Horrible hack to add tracking of locals only to methods which
    // call {open,close}DeterministicBlock.  This is needed for
    // performance, because tracking locals is very expensive.
    private boolean containsDeterministicBlock = false;

    public VisitorForActiveTesting(Visitor visitor) {
        super(visitor);
    }


    public int getStSize() {
        return st.getSize();
    }

    private Stmt getStmtToBeInstrumented(Chain units, AssignStmt assignStmt, Value leftOp) {
        Stmt cur = assignStmt;
        Stmt succ;
        Stmt last = (Stmt) units.getLast();

        while (cur != last) {
            succ = (Stmt) units.getSuccOf(cur);
            if ((succ != null) && (succ instanceof InvokeStmt)) {
                InvokeExpr iExpr = succ.getInvokeExpr();
                if (iExpr.getMethod().getSubSignature().indexOf("<init>") != -1) {
                    if (((InstanceInvokeExpr) iExpr).getBase() == leftOp) {
                        return succ;
                    }
                }
            }
            cur = succ;
        }
        return assignStmt;
    }

    private Stmt getStmtToBeInstrumented2(SootMethod sm, Chain units, AssignStmt assignStmt,
                                          Value objectOnWhichMethodIsInvoked, Stmt currStmtToBeInstrumented) {
        Stmt cur = assignStmt;
        Stmt succ;
        Stmt last = (Stmt) units.getLast();

        if (sm.getSubSignature().indexOf("<init>") != -1 && objectOnWhichMethodIsInvoked != null) {
            while (cur != last) {
                succ = (Stmt) units.getSuccOf(cur);
                if ((succ != null) && (succ instanceof InvokeStmt)) {
                    InvokeExpr iExpr = succ.getInvokeExpr();
                    if (iExpr.getMethod().getSubSignature().indexOf("<init>") != -1) {
                        if (((InstanceInvokeExpr) iExpr).getBase() == objectOnWhichMethodIsInvoked) {
                            return succ;
                        }
                    }
                }
                cur = succ;
            }
        }
        return currStmtToBeInstrumented;
    }

    private Value getMethodsTargetObject(SootMethod sm, Chain units) {
        Value objectOnWhichMethodIsInvoked = null;
        if (!sm.isStatic()) {
            Stmt startStmt = (Stmt) units.getFirst();
            if (startStmt instanceof IdentityStmt) {
                objectOnWhichMethodIsInvoked = ((IdentityStmt) startStmt).getLeftOp();
            } else {
                System.err.println("First statement within a non-static method is not an IdentityStmt");
                System.exit(-1);
            }
        }
        return objectOnWhichMethodIsInvoked;
    }

    public void visitMethodBegin(SootMethod sm, Chain units) {
        nextVisitor.visitMethodBegin(sm, units);

        if (!Parameters.trackDeterministicLocals)
            return;

        containsDeterministicBlock = false;

        for (Object u : units) {
            if (u instanceof InvokeStmt) {
                InvokeStmt is = (InvokeStmt)u;
                InvokeExpr ie = is.getInvokeExpr();

                if (!ie.getMethod().isStatic())
                    continue;

                String sig = ie.getMethod().getSignature();
                if (sig.equals(openDeterministicBlockSig) || sig.equals(closeDeterministicBlockSig)) {
                     containsDeterministicBlock = true;
                 }
            }
        }
    }

    public void visitMethodEnd(SootMethod sm, Chain units) {
        nextVisitor.visitMethodEnd(sm, units);

        if (!Parameters.trackLocals
            && !(Parameters.trackDeterministicLocals && containsDeterministicBlock))
            return;

        if (sm.getName().contains("<clinit>") || sm.getName().contains("<init>"))
            return;

        // Find the first place where it is legal to insert
        // instrumentation calls.
        //
        // (A method beings with some number of identity statements.
        // The first legal place is immediately after the last of
        // these statements.)
        Stmt lastIdStmt = null;
        for (Object u : units) {
            if (!(u instanceof IdentityStmt))
                break;
            lastIdStmt = (Stmt)u;
        }

        // If lastIdStmt is null here, then we can insert
        // instrumentation calls at the beginning of the method body.
        // In this case, however, there are no method parameters (or a
        // "this" variable) to instrument, so we will never
        // dereference lastIdStmt.

        // Insert a call to myWriteAfter for each method parameter.
        Body body = sm.getActiveBody();
        for (int i = sm.getParameterCount() - 1; i >=0 ; i--) {
            Local local = body.getParameterLocal(i);
            if (local == null) {
                System.err.println("Parameter is never assigned to a local in: " + sm);
                System.exit(-1);
            }
            addCallWithLocalValue(units, lastIdStmt, "myWriteAfter", local, false);
        }

        // Insert a call to myWriteAfter for this.
        if (!sm.isStatic()) {
            Local thisLocal = body.getThisLocal();
            if (thisLocal == null) {
                System.err.println("'this' is never assigned to a local in: " + sm);
                System.exit(-1);
            }
            addCallWithLocalValue(units, lastIdStmt, "myWriteAfter", thisLocal, false);
        }
    }

    public void visitStmtAssign(SootMethod sm, Chain units, AssignStmt assignStmt) {
        Value leftOp = assignStmt.getLeftOp();
        Value rightOp = assignStmt.getRightOp();
        if (!Parameters.ignoreAlloc) {
            if ((rightOp instanceof NewExpr)
                    || (rightOp instanceof NewArrayExpr)
                    || (rightOp instanceof NewMultiArrayExpr)) {
                Stmt stmtToBeInstrumented = getStmtToBeInstrumented(units, assignStmt, leftOp);
                Value objectOnWhichMethodIsInvoked = getMethodsTargetObject(sm, units);

                stmtToBeInstrumented = getStmtToBeInstrumented2(sm, units, assignStmt,
                        objectOnWhichMethodIsInvoked, stmtToBeInstrumented);

                if (objectOnWhichMethodIsInvoked != null) {
                    addCallWithObjectObject(units, stmtToBeInstrumented, "myNewExprInANonStaticMethodAfter",
                            leftOp, objectOnWhichMethodIsInvoked, false);
                } else {
                    addCallWithObject(units, stmtToBeInstrumented, "myNewExprInAStaticMethodAfter", leftOp, false);
                }
            }
        }
        nextVisitor.visitStmtAssign(sm, units, assignStmt);
    }

    public void visitStmtEnterMonitor(SootMethod sm, Chain units, EnterMonitorStmt enterMonitorStmt) {
        if (!Parameters.ignoreConcurrency) {
            addCallWithObject(units, enterMonitorStmt, "myLockBefore", enterMonitorStmt.getOp(), true);
        }
        nextVisitor.visitStmtEnterMonitor(sm, units, enterMonitorStmt);
    }

    public void visitStmtExitMonitor(SootMethod sm, Chain units, ExitMonitorStmt exitMonitorStmt) {
        if (!Parameters.ignoreConcurrency) {
            addCallWithObject(units, exitMonitorStmt, "myUnlockAfter", exitMonitorStmt.getOp(), false);
        }
        nextVisitor.visitStmtExitMonitor(sm, units, exitMonitorStmt);
    }

    public void visitInstanceInvokeExpr(SootMethod sm, Chain units, Stmt s, InstanceInvokeExpr invokeExpr, InvokeContext context) {
        Value base = invokeExpr.getBase();
        String sig = invokeExpr.getMethod().getSubSignature();

        if (!Parameters.ignoreConcurrency) {
            if (sig.equals("void wait()")) {
                addCallWithObject(units, s, "myWaitBefore", base, true);
                addCallWithObject(units, s, "myWaitAfter", base, false);
            } else if (sig.equals("void wait(long)") || sig.equals("void wait(long,int)")) {
                addCallWithObject(units, s, "myWaitBefore", base, true);
                addCallWithObject(units, s, "myWaitAfter", base, false);
            } else if (sig.equals("void notify()")) {
                addCallWithObject(units, s, "myNotifyBefore", base, true);
            } else if (sig.equals("void notifyAll()")) {
                addCallWithObject(units, s, "myNotifyAllBefore", base, true);
            } else if (sig.equals("void start()") && isThreadSubType(invokeExpr.getMethod().getDeclaringClass())) {
                addCallWithObject(units, s, "myStartBefore", base, true);
                addCallWithObject(units, s, "myStartAfter", base, false);
            } else if (sig.equals("void join()") && isThreadSubType(invokeExpr.getMethod().getDeclaringClass())) {
                addCallWithObject(units, s, "myJoinAfter", base, false);
            } else if ((sig.equals("void join(long)") || sig.equals("void join(long,int)"))
                       && isThreadSubType(invokeExpr.getMethod().getDeclaringClass())) {
                addCallWithObject(units, s, "myJoinAfter", base, false);
            }
        }

        nextVisitor.visitInstanceInvokeExpr(sm, units, s, invokeExpr, context);

        if (!Parameters.ignoreMethods) {
            addCall(units, s, "myMethodEnterBefore", true);
            addCall(units, s, "myMethodExitAfter", false);
        }

        if (sig.indexOf("<init>") == -1) {
            if (!Parameters.ignoreConcurrency) {
                String ssig = sig.substring(sig.indexOf(' ') + 1);
                Value sig2 = StringConstant.v(ssig);
                addCallWithObjectString(units, s, "myLockBefore", base, sig2, true);
                // t = t.syncMethod() is problematic, so do not pass t
                addCall(units, s, "myUnlockAfter", false);
            }

        } else if (Parameters.trackLocals ||
                   (Parameters.trackDeterministicLocals && containsDeterministicBlock)) {
            // Call to <init> -- add instrumentation call to myWriteAfter().
            //
            // NOTE: This captures assignments to locals of newly
            // allocated objects.  In the bytecode, such assignments
            // look like:
            //     local = new Object;
            //     local.<init>(...);
            if (base instanceof Local) {
                String name = ((Local)base).getName();
                // Must be an InvokeStmt (to be an init call).
                if (!(s instanceof InvokeStmt)) {
                    throw new UnknownASTNodeException();
                }
                if (!name.startsWith("$") && !name.equals("this"))
                    addCallWithLocalValue(units, s, "myWriteAfter", (Local)base, false);
            }
        }
    }

    public void visitStaticInvokeExpr(SootMethod sm, Chain units, Stmt s, StaticInvokeExpr invokeExpr, InvokeContext context) {
        nextVisitor.visitStaticInvokeExpr(sm, units, s, invokeExpr, context);

        if (!Parameters.ignoreMethods) {
            addCall(units, s, "myMethodEnterBefore", true);
            addCall(units, s, "myMethodExitAfter", false);
        }

        if (invokeExpr.getMethod().isSynchronized() && !Parameters.ignoreConcurrency) {
            addCallWithIntString(units, s, "myLockBefore",
                    IntConstant.v(st.get(invokeExpr.getMethod().getDeclaringClass().getName())),
                    StringConstant.v(invokeExpr.getMethod().getDeclaringClass().getName()),true);
            addCallWithInt(units, s, "myUnlockAfter",
                    IntConstant.v(st.get(invokeExpr.getMethod().getDeclaringClass().getName())), false);
        }

        String sig = invokeExpr.getMethod().getSignature();
        if (sig.equals(openDeterministicBlockSig)) {
            addCall(units, s, "myOpenDeterministicBlock", true);
        } else if (sig.equals(closeDeterministicBlockSig)) {
            addCall(units, s, "myCloseDeterministicBlock", true);
        }
    }


    public void visitArrayRef(SootMethod sm, Chain units, Stmt s, ArrayRef arrayRef, RefContext context) {
        if (!Parameters.ignoreArrays) {
            if (context == RHSContextImpl.getInstance()) {
                addCallWithObjectInt(units, s, "myReadBefore", arrayRef.getBase(), arrayRef.getIndex(), true);
            } else {
                addCallWithObjectInt(units, s, "myWriteBefore", arrayRef.getBase(), arrayRef.getIndex(), true);
            }
        }
        nextVisitor.visitArrayRef(sm, units, s, arrayRef, context);
    }

    public void visitInstanceFieldRef(SootMethod sm, Chain units, Stmt s, InstanceFieldRef instanceFieldRef, RefContext context) {
        if (!Parameters.ignoreFields) {
            if ((!sm.getName().equals("<init>") || !instanceFieldRef.getField().getName().equals("this$0"))
            && (!sm.getName().equals("<init>") || !instanceFieldRef.getField().getName().startsWith("val$")))
            {
                Value v = IntConstant.v(st.get(instanceFieldRef.getField().getName()));
                if (Modifier.isVolatile(instanceFieldRef.getField().getModifiers())) {
                    if (context == RHSContextImpl.getInstance()) {
                        addCallWithObjectInt(units, s, "myVReadBefore", instanceFieldRef.getBase(), v, true);
                    } else {
                        addCallWithObjectInt(units, s, "myVWriteBefore", instanceFieldRef.getBase(), v, true);
                    }
                } else {
                    if (context == RHSContextImpl.getInstance()) {
                        addCallWithObjectInt(units, s, "myReadBefore", instanceFieldRef.getBase(), v, true);
                    } else {
                        addCallWithObjectInt(units, s, "myWriteBefore", instanceFieldRef.getBase(), v, true);
                    }
                }
            }
        }
        nextVisitor.visitInstanceFieldRef(sm, units, s, instanceFieldRef, context);
    }

    public static void addCallWithLocalValue(Chain units, Stmt s, String methodName, Local l, boolean before) {
        StringConstant localName = StringConstant.v(l.getName());
        Type type = l.getType();

        if (type instanceof PrimType) {
            addCallWithType(units, s, methodName, localName, l, type.toString(), before);
        } else if (type instanceof RefType) {
            addCallWithType(units, s, methodName, localName, l, "java.lang.Object", before);
        } else if (type instanceof ArrayType) {
            addCallWithType(units, s, methodName, localName, l, "java.lang.Object", before);
        } else if (type.toString().equals("null_type")) {
            addCallWithType(units, s, methodName, localName, l, "java.lang.Object", before);
        }
    }

    public static void addCallWithType(Chain units, Stmt s, String methodName,
                                       Value v1, Value v2,
                                       String typeName, boolean before) {
        SootMethodRef mr;
        LinkedList<Value> args = new LinkedList<Value>();
        args.addLast(IntConstant.v(getAndIncCounter()));
        args.addLast(v1);
        args.addLast(v2);

        if (typeName.equals("java.lang.Object")) {
            args.addLast(StringConstant.v(v2.getType().toString()));
            typeName = "java.lang.Object,java.lang.String";
        }

        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName
                + "(int,java.lang.String," + typeName + ")>").makeRef();

        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        }
    }

    public void visitLocal(SootMethod sm, Chain units, Stmt s, Local local, LocalContext context) {
        if (((context == LHSContextImpl.getInstance())
             // || ((context == ThisRefContextImpl.getInstance()) && (sm.getSubSignature().indexOf("<init>") == -1))
             // || (context == ParameterRefContextImpl.getInstance())
             || (context == NewArrayContextImpl.getInstance())
             || (context == NewMultiArrayContextImpl.getInstance()))
            && (s instanceof DefinitionStmt)) {

            DefinitionStmt ds = (DefinitionStmt) s;
            Value left = ds.getLeftOp();
            Value right = ds.getRightOp();

            if (!(left instanceof Local)) {
                throw new UnknownASTNodeException();
            }

            if (right instanceof NewExpr) {
                // Skip
            } else if (Parameters.trackLocals ||
                       (Parameters.trackDeterministicLocals && containsDeterministicBlock)) {
                if (local.getName().charAt(0) != '$') {
                    addCallWithLocalValue(units, s, "myWriteAfter", local, false);
                }
            }
        }
        nextVisitor.visitLocal(sm, units, s, local, context);
    }

    public void visitStaticFieldRef(SootMethod sm, Chain units, Stmt s, StaticFieldRef staticFieldRef, RefContext context) {
        if (!Parameters.ignoreFields) {
            Value v1 = IntConstant.v(st.get(staticFieldRef.getField().getDeclaringClass().getName()));
            Value v2 = IntConstant.v(st.get(staticFieldRef.getField().getName()));
            if (Modifier.isVolatile(staticFieldRef.getField().getModifiers())) {
                if (context == RHSContextImpl.getInstance()) {
                    addCallWithIntInt(units, s, "myVReadBefore", v1, v2, true);
                } else {
                    addCallWithIntInt(units, s, "myVWriteBefore", v1, v2, true);
                }
            } else {
                if (context == RHSContextImpl.getInstance()) {
                    addCallWithIntInt(units, s, "myReadBefore", v1, v2, true);
                } else {
                    addCallWithIntInt(units, s, "myWriteBefore", v1, v2, true);
                }
            }
        }
        nextVisitor.visitStaticFieldRef(sm, units, s, staticFieldRef, context);
    }


}
