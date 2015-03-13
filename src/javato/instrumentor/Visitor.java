package javato.instrumentor;

import javato.instrumentor.contexts.*;
import javato.activetesting.common.Parameters;
import soot.*;
import soot.jimple.*;
import soot.tagkit.*;
import soot.util.Chain;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Copyright (c) 2007-2008,
 * Koushik Sen    <ksen@cs.berkeley.edu>
 * Pallavi Joshi  <pallavi@cs.berkeley.edu>
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

public class Visitor {
    protected Visitor nextVisitor;
    static private int counter = 0;
    static protected SymbolTables st = new SymbolTables();
    static public String observerClass;// = System.getProperty("javato.call", "javato.observer.Observer");
    static public Stmt thisStmt;
    static private ArrayList<String> iidToLineMap = new ArrayList<String>(10000);
    static public SootClass thisClass;

    public int getCounter() {
        return counter;
    }

    private static int getLineNum(Host h) {
        if (h.hasTag("LineNumberTag")) {
            return ((LineNumberTag) h.getTag("LineNumberTag")).getLineNumber();
        }
        if (h.hasTag("SourceLineNumberTag")) {
            return ((SourceLineNumberTag) h.getTag("SourceLineNumberTag")).getLineNumber();
        }
        if (h.hasTag("SourceLnPosTag")) {
            return ((SourceLnPosTag) h.getTag("SourceLnPosTag")).startLn();
        }
        return 0;
    }

    private static String getFileName(SootClass c) {
        if (!c.hasTag("SourceFileTag"))
            return "unknown.java";
        String s = ((SourceFileTag) c.getTag("SourceFileTag")).getSourceFile();
        String pckgName = c.getPackageName();
        return pckgName.equals("") ? s :
                pckgName.replace('.', '/') + "/" + s;
    }

    public static int getAndIncCounter() {
        iidToLineMap.add(getFileName(thisClass) + ".html#" + getLineNum(thisStmt));
        return counter++;
    }

    public static void setObserverClass(String s) {
        observerClass = s;
    }

    public static void dumpIidToLine() {
        ObjectOutputStream out = null;
        PrintStream out2 = null;
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(Parameters.iidToLineMapFile)));
            out.writeObject(iidToLineMap);
            out.close();
            out2 = new PrintStream(new BufferedOutputStream(new FileOutputStream(Parameters.iidToLineMapFile + ".html")));
            out2.println("<html><body>");
            int i = 0;
            for (String s : iidToLineMap) {
                out2.println("<a href=\"tmpclasses/" + s + "\">" + i + "</a><br>");
                i++;
            }
            out2.println("</html></body>");
            out2.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    public Visitor(Visitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    public void visitMethodBegin(SootMethod sm, Chain units) {
        nextVisitor.visitMethodBegin(sm, units);
    }

    public void visitMethodEnd(SootMethod sm, Chain units) {
        nextVisitor.visitMethodEnd(sm, units);
    }

    public void visitStmt(SootMethod sm, Chain units, Stmt s) {
        nextVisitor.visitStmt(sm, units, s);
    }

    public void visitStmtNop(SootMethod sm, Chain units, NopStmt nopStmt) {
        nextVisitor.visitStmtNop(sm, units, nopStmt);
    }

    public void visitStmtBreakpoint(SootMethod sm, Chain units, BreakpointStmt breakpointStmt) {
        nextVisitor.visitStmtBreakpoint(sm, units, breakpointStmt);
    }/*
     * ThrowStmt ::= 'throw' LocalOrConstant@ThrowContext
     */

    public void visitStmtThrow(SootMethod sm, Chain units, ThrowStmt throwStmt) {
        nextVisitor.visitStmtThrow(sm, units, throwStmt);
    }

    public void visitStmtReturnVoid(SootMethod sm, Chain units, ReturnVoidStmt returnVoidStmt) {
        nextVisitor.visitStmtReturnVoid(sm, units, returnVoidStmt);
    }/*
     * ReturnStmt ::= 'return' LocalOrConstant@ReturnContext
     */

    public void visitStmtReturn(SootMethod sm, Chain units, ReturnStmt returnStmt) {
        nextVisitor.visitStmtReturn(sm, units, returnStmt);
    }/*
     * MonitorStmt ::=  EnterMonitorStmt | ExitMonitorStmt
     */

    public void visitStmtMonitor(SootMethod sm, Chain units, MonitorStmt monitorStmt) {
        nextVisitor.visitStmtMonitor(sm, units, monitorStmt);
    }/*
     * ExitMonitorStmt ::= 'monitorexit' LocalOrConstant@ExitMonitorContext
     */

    public void visitStmtExitMonitor(SootMethod sm, Chain units, ExitMonitorStmt exitMonitorStmt) {
        nextVisitor.visitStmtExitMonitor(sm, units, exitMonitorStmt);
    }/*
     * EnterMonitorStmt ::= 'monitorenter' LocalOrConstant@EnterMonitorContext
     */

    public void visitStmtEnterMonitor(SootMethod sm, Chain units, EnterMonitorStmt enterMonitorStmt) {
        nextVisitor.visitStmtEnterMonitor(sm, units, enterMonitorStmt);
    }/*
     * LookupSwitchStmt ::= LocalOrConstant@LookupSwitchContext
     * (LookupValue@LookupSwitchContext Label@LookupSwitchContext)* Label@LookupSwitchDefaultContext
     */

    public void visitStmtLookupSwitch(SootMethod sm, Chain units, LookupSwitchStmt lookupSwitchStmt) {
        nextVisitor.visitStmtLookupSwitch(sm, units, lookupSwitchStmt);
    }

    public void visitLookupValue(SootMethod sm, Chain units, Stmt stmt, int lookupValue) {
        nextVisitor.visitLookupValue(sm, units, stmt, lookupValue);
    }/*
     * TableSwitchStmt ::= LocalOrConstant@TableSwitchContext
     * (LookupValue@TableSwitchContext Label@TableSwitchContext)* Label@TableSwitchDefaultContext
     */

    public void visitStmtTableSwitch(SootMethod sm, Chain units, TableSwitchStmt tableSwitchStmt) {
        nextVisitor.visitStmtTableSwitch(sm, units, tableSwitchStmt);
    }/*
     * InvokeStmt ::= InvokeExpr@InvokeOnlyContext
     */

    public void visitStmtInvoke(SootMethod sm, Chain units, InvokeStmt invokeStmt) {
        nextVisitor.visitStmtInvoke(sm, units, invokeStmt);
    }

    public void visitStmtIf(SootMethod sm, Chain units, IfStmt ifStmt) {
        nextVisitor.visitStmtIf(sm, units, ifStmt);
    }/*
     * GotoStmt ::= Label@GotoContext
     */

    public void visitStmtGoto(SootMethod sm, Chain units, GotoStmt gotoStmt) {
        nextVisitor.visitStmtGoto(sm, units, gotoStmt);
    }/*
     * IdentityStmt ::= Local@IdentityContext ThisRef@IdentityContext
     * | Local@IdentityContext ParameterRef@IdentityContext | Local@IdentityCntext CaughtExceptionRef@IdentityContext
     */

    public void visitStmtIdentity(SootMethod sm, Chain units, IdentityStmt identityStmt) {
        nextVisitor.visitStmtIdentity(sm, units, identityStmt);
    }/*
     * AssignStmt ::= ConcreteRef@LHSContext LocalOrConstant@RHSContext
     * | Local@LHSContext RHS@LHSContext
     */

    public void visitStmtAssign(SootMethod sm, Chain units, AssignStmt assignStmt) {
        nextVisitor.visitStmtAssign(sm, units, assignStmt);
    }/*
     * RHS{LHSContext} ::= ConcreteRef@RHSContext | LocalOrConstant@RHSContext | Expr@RSHContext
     */

    public void visitRHS(SootMethod sm, Chain units, Stmt s, Value right) {
        nextVisitor.visitRHS(sm, units, s, right);
    }/*
     * Expr{RHSContext} ::= BinopExpr@RHSContext | CastExpr@RHSContext | InstanceOfExpr@RHSContext | InvokeExpr@RHSContext
     * | NewExpr@RHSContext | NewArrayExpr@RHSContext | NewMultiArrayExpr@RHSContext
     * | LengthExpr@RHSContext | NegExpr@RHSContext
     */

    public void visitExpr(SootMethod sm, Chain units, Stmt s, Expr expr) {
        nextVisitor.visitExpr(sm, units, s, expr);
    }/*
     * NegExpr{RHSContext} ::= LocalOrConstant@NegContext
     */

    public void visitNegExpr(SootMethod sm, Chain units, Stmt s, NegExpr negExpr) {
        nextVisitor.visitNegExpr(sm, units, s, negExpr);
    }/*
     * LengthExpr{RHSContext} ::= LocalOrConstant@LengthContext
     */

    public void visitLengthExpr(SootMethod sm, Chain units, Stmt s, LengthExpr lengthExpr) {
        nextVisitor.visitLengthExpr(sm, units, s, lengthExpr);
    }/*
     * NewMultiArrayExpr{RHSContext} ::= Type@NewMultiArrayContext (LocalOrConstant@NewMultiArrayContext)*
     */

    public void visitNewMultiArrayExpr(SootMethod sm, Chain units, Stmt s, NewMultiArrayExpr newMultiArrayExpr) {
        nextVisitor.visitNewMultiArrayExpr(sm, units, s, newMultiArrayExpr);
    }/*
     * NewArrayExpr{RHSContext} ::= Type@NewArrayContext (LocalOrConstant@NewArrayContext)*
     */

    public void visitNewArrayExpr(SootMethod sm, Chain units, Stmt s, NewArrayExpr newArrayExpr) {
        nextVisitor.visitNewArrayExpr(sm, units, s, newArrayExpr);
    }/*
     * NewExpr{RHSContext} ::= Type@NewArrayContext
     */

    public void visitNewExpr(SootMethod sm, Chain units, Stmt s, NewExpr newExpr) {
        nextVisitor.visitNewExpr(sm, units, s, newExpr);
    }/*
     * InvokeExpr{InvokeAndAssignContext,InvokeOnlyContext} ::= LocalOrConstant@InvokeAndAssignTargetContextImpl Signature@InvokeAndAssignContext
     *                                                              (LocalOrConstant@InvokeAndAssignArgumentContext)*
     *                                                          | LocalOrConstant@InvokeOnlyTargetContext Signature@InvokeOnlyContext
     *                                                              (LocalOrConstant@InvokeOnlyArgumentContext)*
     */

    public void visitInvokeExpr(SootMethod sm, Chain units, Stmt s, InvokeExpr invokeExpr, InvokeContext context) {
        nextVisitor.visitInvokeExpr(sm, units, s, invokeExpr, context);
    }/*
     * InstanceOfExpr{RHSContext} ::= LocalOrConstant@InstanceOfContext Type@InstanceOfContext
     */

    public void visitStaticInvokeExpr(SootMethod sm, Chain units, Stmt s, StaticInvokeExpr invokeExpr, InvokeContext context) {
        nextVisitor.visitStaticInvokeExpr(sm, units, s, invokeExpr, context);
    }


    public void visitInstanceInvokeExpr(SootMethod sm, Chain units, Stmt s, InstanceInvokeExpr invokeExpr, InvokeContext context) {
        nextVisitor.visitInstanceInvokeExpr(sm, units, s, invokeExpr, context);
    }

    public void visitInstanceOfExpr(SootMethod sm, Chain units, Stmt s, InstanceOfExpr instanceOfExpr) {
        nextVisitor.visitInstanceOfExpr(sm, units, s, instanceOfExpr);
    }/*
     * CastExpr{RHSContext} ::= Type@CastContext LocalOrConstant@CastContext
     */

    public void visitCastExpr(SootMethod sm, Chain units, Stmt s, CastExpr castExpr) {
        nextVisitor.visitCastExpr(sm, units, s, castExpr);
    }/*
     * Type{CastContext,InstanceOfContext,NewArrayContext,NewExpr,NewMultiArrayContext}
     */

    public void visitType(SootMethod sm, Chain units, Stmt s, Type castType, TypeContext context) {
        nextVisitor.visitType(sm, units, s, castType, context);
    }/*
     * BinopExpr{RHSContext,IfContext} ::= LocalOrConstant@RHSFirstContext Binop@RHSContext LocalOrConstant@RHSSecondContext
     * | LocalOrConstant@IfFirstContext Binop@IfContext  LocalOrConstant@IfSecondContext
     */

    public void visitBinopExpr(SootMethod sm, Chain units, Stmt s, BinopExpr expr, BinopExprContext context) {
        nextVisitor.visitBinopExpr(sm, units, s, expr, context);
    }/*
     * ConcreteRef{RHSContext,LHSContext} ::= InstanceFieldRef{RHSContext} | ArrayRef{RHSContext} | StaticFieldRef{RHSContext}
     * | InstanceFieldRef{LHSContext} | ArrayRef{LHSContext} | StaticFieldRef{LHSContext}
     */

    public void visitConcreteRef(SootMethod sm, Chain units, Stmt s, ConcreteRef concreteRef, RefContext context) {
        nextVisitor.visitConcreteRef(sm, units, s, concreteRef, context);
    }/*
     * LocalOrConstant{RHSFirstContext,RHSSecondContext,IfFirstContext,IfSecondContext,CastContext,InstanceOfContext,
     * InvokeAndAssignTargetContextImpl,InvokeAndAssignArgumentContext,InvokeOnlyTargetContext,InvokeOnlyArgumentContext,
     * LengthContext,NegContext,NewMultiArrayContext,NewArrayContext,
     * RHSContext,EnterMonitorContext,ExitMonitorContext,LookupSwitchContext,TableSwitchContext,
     * ReturnContext,ThrowContext}  ::= Local | Constant
     */

    public void visitLocalOrConstant(SootMethod sm, Chain units, Stmt s, Value right, LocalOrConstantContext context) {
        nextVisitor.visitLocalOrConstant(sm, units, s, right, context);
    }/*
     * Constant{{RHSFirstContext,RHSSecondContext,IfFirstContext,IfSecondContext,CastContext,InstanceOfContext,
     * InvokeAndAssignTargetContextImpl,InvokeAndAssignArgumentContext,InvokeOnlyTargetContext,InvokeOnlyArgumentContext,
     * LengthContext,NegContext,NewMultiArrayContext,NewArrayContext,
     * RHSContext,EnterMonitorContext,ExitMonitorContext,LookupSwitchContext,TableSwitchContext,
     * ReturnContext,ThrowContext}
     */

    public void visitConstant(SootMethod sm, Chain units, Stmt s, Constant constant, LocalOrConstantContext context) {
        nextVisitor.visitConstant(sm, units, s, constant, context);
    }/*
     * Local{RHSFirstContext,RHSSecondContext,IfFirstContext,IfSecondContext,CastContext,InstanceOfContext,
     * InvokeAndAssignTargetContextImpl,InvokeAndAssignArgumentContext,InvokeOnlyTargetContext,InvokeOnlyArgumentContext,
     * LengthContext,NegContext,NewMultiArrayContext,NewArrayContext,
     * RHSContext,EnterMonitorContext,ExitMonitorContext,LookupSwitchContext,TableSwitchContext,
     * ReturnContext,ThrowContext,IdentityContext,LHSContext}
     */

    public void visitLocal(SootMethod sm, Chain units, Stmt s, Local local, LocalContext context) {
        nextVisitor.visitLocal(sm, units, s, local, context);
    }/*
     * StaticFieldRef{RHSContext,LHSContext}
     */

    public void visitStaticFieldRef(SootMethod sm, Chain units, Stmt s, StaticFieldRef staticFieldRef, RefContext context) {
        nextVisitor.visitStaticFieldRef(sm, units, s, staticFieldRef, context);
    }/*
     * ArrayRef{RHSContext,LHSContext}
     */

    public void visitArrayRef(SootMethod sm, Chain units, Stmt s, ArrayRef arrayRef, RefContext context) {
        nextVisitor.visitArrayRef(sm, units, s, arrayRef, context);
    }/*
     * InstanceFieldRef{RHSContext,LHSContext}
     */

    public void visitInstanceFieldRef(SootMethod sm, Chain units, Stmt s, InstanceFieldRef instanceFieldRef, RefContext context) {
        nextVisitor.visitInstanceFieldRef(sm, units, s, instanceFieldRef, context);
    }/*
     * CaughtExceptionRef{IdentityContext}
     */

    public void visitCaughtExceptionRef(SootMethod sm, Chain units, IdentityStmt s, CaughtExceptionRef caughtExceptionRef) {
        nextVisitor.visitCaughtExceptionRef(sm, units, s, caughtExceptionRef);
    }/*
     * ParameterRef{IdentityContext}
     */

    public void visitParameterRef(SootMethod sm, Chain units, IdentityStmt s, ParameterRef parameterRef) {
        nextVisitor.visitParameterRef(sm, units, s, parameterRef);
    }/*
     * ThisRef{IdentityContext}
     */

    public void visitThisRef(SootMethod sm, Chain units, IdentityStmt s, ThisRef thisRef) {
        nextVisitor.visitThisRef(sm, units, s, thisRef);
    }/*
     * Binop{RHSContext,IfContext}
     */

    public void visitBinop(SootMethod sm, Chain units, Stmt s, String op, BinopExprContext context) {
        nextVisitor.visitBinop(sm, units, s, op, context);
    }/*
     * Signature{InvokeAndAssignContext,InvokeOnlyContext}
     */

    public void visitSignature(SootMethod sm, Chain units, Stmt s, String signature, InvokeContext context) {
        nextVisitor.visitSignature(sm, units, s, signature, context);
    }/*
     * Label{GotoContext,IfContext,LookupSwitchContext,LookupSwitchDefaultContext,TableSwitchContext,TableSwitchDefaultContext}
     */

    public void visitLabel(SootMethod sm, Chain units, Stmt gotoStmt, Unit target, LabelContext context) {
        nextVisitor.visitLabel(sm, units, gotoStmt, target, context);
    }

    protected void addCallLast(Chain units, String methodName) {
        SootMethodRef mr;

        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int)>").makeRef();
        units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, IntConstant.v(getAndIncCounter()))), units.getLast());
    }

    protected void addCallLastWithObject(Chain units, String methodName, Value v) {
        SootMethodRef mr;

        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int,java.lang.Object)>").makeRef();
        units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, IntConstant.v(getAndIncCounter()), v)), units.getLast());
    }


    protected void addCall(Chain units, Stmt s, String methodName, boolean before) {
        SootMethodRef mr;

        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, IntConstant.v(getAndIncCounter()))), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, IntConstant.v(getAndIncCounter()))), s);
        }
    }

    protected void addCallWithObject(Chain units, Stmt s, String methodName, Value v, boolean before) {
        SootMethodRef mr;

        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int,java.lang.Object)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, IntConstant.v(getAndIncCounter()), v)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, IntConstant.v(getAndIncCounter()), v)), s);
        }
    }

    protected void addCallWithInt(Chain units, Stmt s, String methodName, Value v, boolean before) {
        SootMethodRef mr;

        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int,int)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, IntConstant.v(getAndIncCounter()), v)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, IntConstant.v(getAndIncCounter()), v)), s);
        }
    }

    protected void addCallWithObjectInt(Chain units, Stmt s, String methodName, Value v1, Value v2, boolean before) {
        SootMethodRef mr;

        LinkedList args = new LinkedList();
        args.addLast(IntConstant.v(getAndIncCounter()));
        args.addLast(v1);
        args.addLast(v2);
        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int,java.lang.Object,int)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        }
    }

    protected void addCallWithIntObject(Chain units, Stmt s, String methodName, Value v1, Value v2, boolean before) {
        SootMethodRef mr;

        LinkedList args = new LinkedList();
        args.addLast(IntConstant.v(getAndIncCounter()));
        args.addLast(v1);
        args.addLast(v2);
        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int,int,java.lang.Object)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        }
    }

    protected void addCallWithIntString(Chain units, Stmt s, String methodName, Value v1, Value v2, boolean before) {
        SootMethodRef mr;

        LinkedList args = new LinkedList();
        args.addLast(IntConstant.v(getAndIncCounter()));
        args.addLast(v1);
        args.addLast(v2);
        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int,int,java.lang.String)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        }
    }

    protected void addCallWithObjectObject(Chain units, Stmt s, String methodName, Value v1, Value v2, boolean before) {
        SootMethodRef mr;
        LinkedList args = new LinkedList();
        args.addLast(IntConstant.v(getAndIncCounter()));
        args.addLast(v1);
        args.addLast(v2);
        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int,java.lang.Object,java.lang.Object)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        }
    }

    protected void addCallWithObjectString(Chain units, Stmt s, String methodName, Value v1, Value v2, boolean before) {
        SootMethodRef mr;

        LinkedList args = new LinkedList();
        args.addLast(IntConstant.v(getAndIncCounter()));
        args.addLast(v1);
        args.addLast(v2);
        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int,java.lang.Object,java.lang.String)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        }
    }

    protected void addCallWithObjectStringStringInt(Chain units, Stmt s, String methodName,
                                                    Value v1, Value v2, Value v3, Value v4, boolean before) {
        SootMethodRef mr;

        LinkedList args = new LinkedList();
        args.addLast(IntConstant.v(getAndIncCounter()));
        args.addLast(v1);
        args.addLast(v2);
        args.addLast(v3);
        args.addLast(v4);
        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName
                + "(int,java.lang.Object,java.lang.String,java.lang.String,int)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        }
    }

    protected void addCallWithObjectStringString(Chain units, Stmt s, String methodName,
                                                 Value v1, Value v2, Value v3, boolean before) {
        SootMethodRef mr;

        LinkedList args = new LinkedList();
        args.addLast(IntConstant.v(getAndIncCounter()));
        args.addLast(v1);
        args.addLast(v2);
        args.addLast(v3);
        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName
                + "(int,java.lang.Object,java.lang.String,java.lang.String)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        }
    }


    protected void addCallWithObjectIntString(Chain units, Stmt s, String methodName, Value v1, Value v2, Value v3, boolean before) {
        SootMethodRef mr;

        LinkedList args = new LinkedList();
        args.addLast(IntConstant.v(getAndIncCounter()));
        args.addLast(v1);
        args.addLast(v2);
        args.addLast(v3);
        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName
                + "(int,java.lang.Object,int,java.lang.String)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        }
    }

    protected void addCallWithIntInt(Chain units, Stmt s, String methodName, Value v1, Value v2, boolean before) {
        SootMethodRef mr;

        LinkedList args = new LinkedList();
        args.addLast(IntConstant.v(getAndIncCounter()));
        args.addLast(v1);
        args.addLast(v2);
        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int,int,int)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        }
    }

    protected void addCallWithObjectIntInt(Chain units, Stmt s, String methodName, Value v1, Value v2, Value v3, boolean before) {
        SootMethodRef mr;

        LinkedList args = new LinkedList();
        args.addLast(IntConstant.v(getAndIncCounter()));
        args.addLast(v1);
        args.addLast(v2);
        args.addLast(v3);
        mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(int,java.lang.Object,int,int)>").makeRef();
        if (before) {
            units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        } else {
            units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
        }
    }

    protected static boolean isThreadSubType(SootClass c) {
        if (c.getName().equals("java.lang.Thread"))
            return true;
        if (!c.hasSuperclass()) {
            return false;
        }
        return isThreadSubType(c.getSuperclass());
    }

    protected static boolean isRunnableSubType(SootClass c) {
        if (c.implementsInterface("java.lang.Runnable"))
            return true;
        if (c.hasSuperclass())
            return isRunnableSubType(c.getSuperclass());
        return false;
    }

    protected boolean isSubClass(SootClass c, String typeName) {
        if (c.getName().equals(typeName))
            return true;
        if (c.implementsInterface(typeName))
            return true;
        if (!c.hasSuperclass()) {
            return false;
        }
        return isSubClass(c.getSuperclass(), typeName);
    }

    public void writeSymTblSize() {
        writeInteger(Parameters.usedObjectId, st.getSize());
    }

    public static void writeInteger(String file, int val) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            pw.println(val);
            pw.close();
        } catch (IOException e) {
            System.err.println("Error while writing to " + file);
            System.exit(1);
        }

    }
}
