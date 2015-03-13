package javato.instrumentor;

import javato.instrumentor.contexts.*;
import soot.*;
import soot.jimple.*;
import soot.util.Chain;

import java.util.List;

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
public class RecursiveVisitor extends Visitor {

    public RecursiveVisitor(Visitor nextVisitor) {
        super(nextVisitor);
    }


    public void setNextVisitor(Visitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    public void visitMethodBegin(SootMethod sm, Chain units) {

    }

    public void visitMethodEnd(SootMethod sm, Chain units) {

    }

    public void visitStmt(SootMethod sm, Chain units, Stmt s) {
        if (s instanceof AssignStmt) {
            nextVisitor.visitStmtAssign(sm, units, (AssignStmt) s);
        } else if (s instanceof IdentityStmt) {
            nextVisitor.visitStmtIdentity(sm, units, (IdentityStmt) s);
        } else if (s instanceof GotoStmt) {
            nextVisitor.visitStmtGoto(sm, units, (GotoStmt) s);
        } else if (s instanceof IfStmt) {
            nextVisitor.visitStmtIf(sm, units, (IfStmt) s);
        } else if (s instanceof InvokeStmt) {
            nextVisitor.visitStmtInvoke(sm, units, (InvokeStmt) s);
        } else if (s instanceof TableSwitchStmt) {
            nextVisitor.visitStmtTableSwitch(sm, units, (TableSwitchStmt) s);
        } else if (s instanceof LookupSwitchStmt) {
            nextVisitor.visitStmtLookupSwitch(sm, units, (LookupSwitchStmt) s);
        } else if (s instanceof MonitorStmt) {
            nextVisitor.visitStmtMonitor(sm, units, (MonitorStmt) s);
        } else if (s instanceof ReturnStmt) {
            nextVisitor.visitStmtReturn(sm, units, (ReturnStmt) s);
        } else if (s instanceof ReturnVoidStmt) {
            nextVisitor.visitStmtReturnVoid(sm, units, (ReturnVoidStmt) s);
        } else if (s instanceof ThrowStmt) {
            nextVisitor.visitStmtThrow(sm, units, (ThrowStmt) s);
        } else if (s instanceof BreakpointStmt) {
            nextVisitor.visitStmtBreakpoint(sm, units, (BreakpointStmt) s);
        } else if (s instanceof NopStmt) {
            nextVisitor.visitStmtNop(sm, units, (NopStmt) s);
        } else {
            throw new UnknownASTNodeException();
        }
    }

    public void visitStmtNop(SootMethod sm, Chain units, NopStmt nopStmt) {

    }

    public void visitStmtBreakpoint(SootMethod sm, Chain units, BreakpointStmt breakpointStmt) {

    }/*
     * ThrowStmt ::= 'throw' LocalOrConstant@ThrowContext
     */

    public void visitStmtThrow(SootMethod sm, Chain units, ThrowStmt throwStmt) {
        nextVisitor.visitLocalOrConstant(sm, units, throwStmt, throwStmt.getOp(), ThrowContextImpl.getInstance());
    }

    public void visitStmtReturnVoid(SootMethod sm, Chain units, ReturnVoidStmt returnVoidStmt) {

    }/*
     * ReturnStmt ::= 'return' LocalOrConstant@ReturnContext
     */

    public void visitStmtReturn(SootMethod sm, Chain units, ReturnStmt returnStmt) {
        nextVisitor.visitLocalOrConstant(sm, units, returnStmt, returnStmt.getOp(), ReturnContextImpl.getInstance());
    }/*
     * MonitorStmt ::=  EnterMonitorStmt | ExitMonitorStmt
     */

    public void visitStmtMonitor(SootMethod sm, Chain units, MonitorStmt monitorStmt) {
        if (monitorStmt instanceof EnterMonitorStmt) {
            nextVisitor.visitStmtEnterMonitor(sm, units, (EnterMonitorStmt) monitorStmt);
        } else if (monitorStmt instanceof ExitMonitorStmt) {
            nextVisitor.visitStmtExitMonitor(sm, units, (ExitMonitorStmt) monitorStmt);
        } else {
            throw new UnknownASTNodeException();
        }

    }/*
     * ExitMonitorStmt ::= 'monitorexit' LocalOrConstant@ExitMonitorContext
     */

    public void visitStmtExitMonitor(SootMethod sm, Chain units, ExitMonitorStmt exitMonitorStmt) {
        nextVisitor.visitLocalOrConstant(sm, units, exitMonitorStmt, exitMonitorStmt.getOp(), ExitMonitorContextImpl.getInstance());
    }/*
     * EnterMonitorStmt ::= 'monitorenter' LocalOrConstant@EnterMonitorContext
     */

    public void visitStmtEnterMonitor(SootMethod sm, Chain units, EnterMonitorStmt enterMonitorStmt) {
        nextVisitor.visitLocalOrConstant(sm, units, enterMonitorStmt, enterMonitorStmt.getOp(), EnterMonitorContextImpl.getInstance());
    }/*
     * LookupSwitchStmt ::= LocalOrConstant@LookupSwitchContext
     * (LookupValue@LookupSwitchContext Label@LookupSwitchContext)* Label@LookupSwitchDefaultContext
     */

    public void visitStmtLookupSwitch(SootMethod sm, Chain units, LookupSwitchStmt lookupSwitchStmt) {
        nextVisitor.visitLocalOrConstant(sm, units, lookupSwitchStmt, lookupSwitchStmt.getKey(), LookupSwitchContextImpl.getInstance());
        int sz = lookupSwitchStmt.getLookupValues().size();
        for (int i = 0; i < sz; i++) {
            nextVisitor.visitLookupValue(sm, units, lookupSwitchStmt, lookupSwitchStmt.getLookupValue(i));
            nextVisitor.visitLabel(sm, units, lookupSwitchStmt, lookupSwitchStmt.getTarget(i), LookupSwitchLabelContextImpl.getInstance());
        }
        nextVisitor.visitLabel(sm, units, lookupSwitchStmt, lookupSwitchStmt.getDefaultTarget(), LookupSwitchDefaultContextImpl.getInstance());
    }

    public void visitLookupValue(SootMethod sm, Chain units, Stmt stmt, int lookupValue) {

    }/*
     * TableSwitchStmt ::= LocalOrConstant@TableSwitchContext
     * (LookupValue@TableSwitchContext Label@TableSwitchContext)* Label@TableSwitchDefaultContext
     */

    public void visitStmtTableSwitch(SootMethod sm, Chain units, TableSwitchStmt tableSwitchStmt) {
        nextVisitor.visitLocalOrConstant(sm, units, tableSwitchStmt, tableSwitchStmt.getKey(), TableSwitchContextImpl.getInstance());
        int i = 0, val;
        int high = tableSwitchStmt.getHighIndex();
        for (val = tableSwitchStmt.getLowIndex(); val <= high; val++) {
            nextVisitor.visitLookupValue(sm, units, tableSwitchStmt, val);
            nextVisitor.visitLabel(sm, units, tableSwitchStmt, tableSwitchStmt.getTarget(i), TableSwitchLabelContextImpl.getInstance());
            i++;
        }
        nextVisitor.visitLabel(sm, units, tableSwitchStmt, tableSwitchStmt.getDefaultTarget(), TableSwitchDefaultContextImpl.getInstance());
    }/*
     * InvokeStmt ::= InvokeExpr@InvokeOnlyContext
     */

    public void visitStmtInvoke(SootMethod sm, Chain units, InvokeStmt invokeStmt) {
        nextVisitor.visitInvokeExpr(sm, units, invokeStmt, invokeStmt.getInvokeExpr(), InvokeOnlyContextImpl.getInstance());
    }

    public void visitStmtIf(SootMethod sm, Chain units, IfStmt ifStmt) {
        nextVisitor.visitBinopExpr(sm, units, ifStmt, (BinopExpr) ifStmt.getCondition(), IfContextImpl.getInstance());
        nextVisitor.visitLabel(sm, units, ifStmt, ifStmt.getTarget(), IfContextImpl.getInstance());
    }/*
     * GotoStmt ::= Label@GotoContext
     */

    public void visitStmtGoto(SootMethod sm, Chain units, GotoStmt gotoStmt) {
        nextVisitor.visitLabel(sm, units, gotoStmt, gotoStmt.getTarget(), GotoContextImpl.getInstance());
    }/*
     * IdentityStmt ::= Local@IdentityContext ThisRef@IdentityContext
     * | Local@IdentityContext ParameterRef@IdentityContext | Local@IdentityCntext CaughtExceptionRef@IdentityContext
     */

    public void visitStmtIdentity(SootMethod sm, Chain units, IdentityStmt identityStmt) {
        Value left = identityStmt.getLeftOp();
        Value right = identityStmt.getRightOp();
        if (right instanceof ThisRef) {
            nextVisitor.visitLocal(sm, units, identityStmt, (Local) left, ThisRefContextImpl.getInstance());
            nextVisitor.visitThisRef(sm, units, identityStmt, (ThisRef) right);
        } else if (right instanceof ParameterRef) {
            nextVisitor.visitLocal(sm, units, identityStmt, (Local) left, ParameterRefContextImpl.getInstance());
            nextVisitor.visitParameterRef(sm, units, identityStmt, (ParameterRef) right);
        } else if (right instanceof CaughtExceptionRef) {
            nextVisitor.visitLocal(sm, units, identityStmt, (Local) left,
                    CaughtExceptionRefContextImpl.getInstance());
            nextVisitor.visitCaughtExceptionRef(sm, units, identityStmt, (CaughtExceptionRef) right);
        } else {
            throw new UnknownASTNodeException();
        }
    }/*
     * AssignStmt ::= ConcreteRef@LHSContext LocalOrConstant@RHSContext
     * | Local@LHSContext RHS@LHSContext
     */

    public void visitStmtAssign(SootMethod sm, Chain units, AssignStmt assignStmt) {
        Value left = assignStmt.getLeftOp();
        Value right = assignStmt.getRightOp();
        if (left instanceof ConcreteRef) {
            nextVisitor.visitConcreteRef(sm, units, assignStmt, (ConcreteRef) left, LHSContextImpl.getInstance());
            nextVisitor.visitLocalOrConstant(sm, units, assignStmt, right, RHSContextImpl.getInstance());
        } else if (left instanceof Local) {
            nextVisitor.visitLocal(sm, units, assignStmt, (Local) left, LHSContextImpl.getInstance());
            nextVisitor.visitRHS(sm, units, assignStmt, right);
        } else {
            throw new UnknownASTNodeException();
        }
    }/*
     * RHS{LHSContext} ::= ConcreteRef@RHSContext | LocalOrConstant@RHSContext | Expr@RSHContext
     */

    public void visitRHS(SootMethod sm, Chain units, Stmt s, Value right) {
        if (right instanceof ConcreteRef)
            nextVisitor.visitConcreteRef(sm, units, s, (ConcreteRef) right, RHSContextImpl.getInstance());
        else if (right instanceof Expr)
            nextVisitor.visitExpr(sm, units, s, (Expr) right);
        else
            nextVisitor.visitLocalOrConstant(sm, units, s, right, RHSContextImpl.getInstance());
    }/*
     * Expr{RHSContext} ::= BinopExpr@RHSContext | CastExpr@RHSContext | InstanceOfExpr@RHSContext | InvokeExpr@RHSContext
     * | NewExpr@RHSContext | NewArrayExpr@RHSContext | NewMultiArrayExpr@RHSContext
     * | LengthExpr@RHSContext | NegExpr@RHSContext
     */

    public void visitExpr(SootMethod sm, Chain units, Stmt s, Expr expr) {
        if (expr instanceof BinopExpr) {
            nextVisitor.visitBinopExpr(sm, units, s, (BinopExpr) expr, RHSContextImpl.getInstance());
        } else if (expr instanceof CastExpr) {
            nextVisitor.visitCastExpr(sm, units, s, (CastExpr) expr);
        } else if (expr instanceof InstanceOfExpr) {
            nextVisitor.visitInstanceOfExpr(sm, units, s, (InstanceOfExpr) expr);
        } else if (expr instanceof InvokeExpr) {
            nextVisitor.visitInvokeExpr(sm, units, s, (InvokeExpr) expr, InvokeAndAssignContextImpl.getInstance());
        } else if (expr instanceof NewExpr) {
            nextVisitor.visitNewExpr(sm, units, s, (NewExpr) expr);
        } else if (expr instanceof NewArrayExpr) {
            nextVisitor.visitNewArrayExpr(sm, units, s, (NewArrayExpr) expr);
        } else if (expr instanceof NewMultiArrayExpr) {
            nextVisitor.visitNewMultiArrayExpr(sm, units, s, (NewMultiArrayExpr) expr);
        } else if (expr instanceof LengthExpr) {
            nextVisitor.visitLengthExpr(sm, units, s, (LengthExpr) expr);
        } else if (expr instanceof NegExpr) {
            nextVisitor.visitNegExpr(sm, units, s, (NegExpr) expr);
        } else {
            throw new UnknownASTNodeException();
        }
    }/*
     * NegExpr{RHSContext} ::= LocalOrConstant@NegContext
     */

    public void visitNegExpr(SootMethod sm, Chain units, Stmt s, NegExpr negExpr) {
        nextVisitor.visitLocalOrConstant(sm, units, s, negExpr.getOp(), NegContextImpl.getInstance());
    }/*
     * LengthExpr{RHSContext} ::= LocalOrConstant@LengthContext
     */

    public void visitLengthExpr(SootMethod sm, Chain units, Stmt s, LengthExpr lengthExpr) {
        nextVisitor.visitLocalOrConstant(sm, units, s, lengthExpr.getOp(), LengthContextImpl.getInstance());
    }/*
     * NewMultiArrayExpr{RHSContext} ::= Type@NewMultiArrayContext (LocalOrConstant@NewMultiArrayContext)*
     */

    public void visitNewMultiArrayExpr(SootMethod sm, Chain units, Stmt s, NewMultiArrayExpr newMultiArrayExpr) {
        nextVisitor.visitType(sm, units, s, newMultiArrayExpr.getBaseType(), NewMultiArrayContextImpl.getInstance());
        List sizes = newMultiArrayExpr.getSizes();
        for (Object size : sizes) {
            Value v = (Value) size;
            nextVisitor.visitLocalOrConstant(sm, units, s, v, NewMultiArrayContextImpl.getInstance());
        }
    }/*
     * NewArrayExpr{RHSContext} ::= Type@NewArrayContext (LocalOrConstant@NewArrayContext)*
     */

    public void visitNewArrayExpr(SootMethod sm, Chain units, Stmt s, NewArrayExpr newArrayExpr) {
        nextVisitor.visitType(sm, units, s, newArrayExpr.getBaseType(), NewArrayContextImpl.getInstance());
        nextVisitor.visitLocalOrConstant(sm, units, s, newArrayExpr.getSize(), NewArrayContextImpl.getInstance());
    }/*
     * NewExpr{RHSContext} ::= Type@NewArrayContext
     */

    public void visitNewExpr(SootMethod sm, Chain units, Stmt s, NewExpr newExpr) {
        nextVisitor.visitType(sm, units, s, newExpr.getBaseType(), NewExprContextImpl.getInstance());
    }/*
     * InvokeExpr{InvokeAndAssignContext,InvokeOnlyContext} ::= LocalOrConstant@InvokeAndAssignTargetContextImpl Signature@InvokeAndAssignContext
     *                                                              (LocalOrConstant@InvokeAndAssignArgumentContext)*
     *                                                          | LocalOrConstant@InvokeOnlyTargetContext Signature@InvokeOnlyContext
     *                                                              (LocalOrConstant@InvokeOnlyArgumentContext)*
     */

    public void visitInvokeExpr(SootMethod sm, Chain units, Stmt s, InvokeExpr invokeExpr, InvokeContext context) {
        if (invokeExpr instanceof InstanceInvokeExpr) {
            nextVisitor.visitInstanceInvokeExpr(sm, units, s, (InstanceInvokeExpr) invokeExpr, context);
        } else if (invokeExpr instanceof StaticInvokeExpr) {
            nextVisitor.visitStaticInvokeExpr(sm, units, s, (StaticInvokeExpr) invokeExpr, context);
        } else {
            throw new UnknownASTNodeException();
        }
    }

    public void visitStaticInvokeExpr(SootMethod sm, Chain units, Stmt s, StaticInvokeExpr invokeExpr, InvokeContext context) {
        LocalOrConstantContext context2;

        if (context == InvokeAndAssignContextImpl.getInstance()) {
            context2 = InvokeAndAssignArgumentContextImpl.getInstance();
        } else { // instanceof InvokeOnlyContextImpl
            context2 = InvokeOnlyArgumentContextImpl.getInstance();
        }

        nextVisitor.visitSignature(sm, units, s, invokeExpr.getMethodRef().getSignature(), context);
        List args = invokeExpr.getArgs();
        for (Object arg : args) {
            Value value = (Value) arg;
            nextVisitor.visitLocalOrConstant(sm, units, s, value, context2);
        }
    }

    public void visitInstanceInvokeExpr(SootMethod sm, Chain units, Stmt s, InstanceInvokeExpr invokeExpr, InvokeContext context) {
        LocalOrConstantContext context1, context2;

        if (context == InvokeAndAssignContextImpl.getInstance()) {
            context1 = InvokeAndAssignTargetContextImpl.getInstance();
            context2 = InvokeAndAssignArgumentContextImpl.getInstance();
        } else { // instanceof InvokeOnlyContextImpl
            context1 = InvokeOnlyTargetContextImpl.getInstance();
            context2 = InvokeOnlyArgumentContextImpl.getInstance();
        }

        nextVisitor.visitLocalOrConstant(sm, units, s, ((InstanceInvokeExpr) invokeExpr).getBase(), context1);
        nextVisitor.visitSignature(sm, units, s, invokeExpr.getMethodRef().getSignature(), context);
        List args = invokeExpr.getArgs();
        for (Object arg : args) {
            Value value = (Value) arg;
            nextVisitor.visitLocalOrConstant(sm, units, s, value, context2);
        }
    }

    /*
     * InstanceOfExpr{RHSContext} ::= LocalOrConstant@InstanceOfContext Type@InstanceOfContext
     */

    public void visitInstanceOfExpr(SootMethod sm, Chain units, Stmt s, InstanceOfExpr instanceOfExpr) {
        nextVisitor.visitLocalOrConstant(sm, units, s, instanceOfExpr.getOp(), InstanceOfContextImpl.getInstance());
        nextVisitor.visitType(sm, units, s, instanceOfExpr.getCheckType(), InstanceOfContextImpl.getInstance());
    }/*
     * CastExpr{RHSContext} ::= Type@CastContext LocalOrConstant@CastContext
     */

    public void visitCastExpr(SootMethod sm, Chain units, Stmt s, CastExpr castExpr) {
        nextVisitor.visitType(sm, units, s, castExpr.getCastType(), CastContextImpl.getInstance());
        nextVisitor.visitLocalOrConstant(sm, units, s, castExpr.getOp(), CastContextImpl.getInstance());
    }/*
     * Type{CastContext,InstanceOfContext,NewArrayContext,NewExpr,NewMultiArrayContext}
     */

    public void visitType(SootMethod sm, Chain units, Stmt s, Type castType, TypeContext context) {

    }/*
     * BinopExpr{RHSContext,IfContext} ::= LocalOrConstant@RHSFirstContext Binop@RHSContext LocalOrConstant@RHSSecondContext
     * | LocalOrConstant@IfFirstContext Binop@IfContext  LocalOrConstant@IfSecondContext
     */

    public void visitBinopExpr(SootMethod sm, Chain units, Stmt s, BinopExpr expr, BinopExprContext context) {
        Value left = expr.getOp1();
        Value right = expr.getOp2();
        String op = expr.getSymbol();
        LocalOrConstantContext context1, context2;
        if (context instanceof RHSContext) {
            context1 = RHSFirstContextImpl.getInstance();
            context2 = RHSSecondContextImpl.getInstance();
        } else { // instanceof IfContext
            context1 = IfFirstContextImpl.getInstance();
            context2 = IfSecondContextImpl.getInstance();
        }
        nextVisitor.visitLocalOrConstant(sm, units, s, left, context1);
        nextVisitor.visitBinop(sm, units, s, op, context);
        nextVisitor.visitLocalOrConstant(sm, units, s, right, context2);
    }/*
     * ConcreteRef{RHSContext,LHSContext} ::= InstanceFieldRef{RHSContext} | ArrayRef{RHSContext} | StaticFieldRef{RHSContext}
     * | InstanceFieldRef{LHSContext} | ArrayRef{LHSContext} | StaticFieldRef{LHSContext}
     */

    public void visitConcreteRef(SootMethod sm, Chain units, Stmt s, ConcreteRef concreteRef, RefContext context) {
        if (concreteRef instanceof InstanceFieldRef)
            nextVisitor.visitInstanceFieldRef(sm, units, s, (InstanceFieldRef) concreteRef, context);
        else if (concreteRef instanceof ArrayRef)
            nextVisitor.visitArrayRef(sm, units, s, (ArrayRef) concreteRef, context);
        else if (concreteRef instanceof StaticFieldRef)
            nextVisitor.visitStaticFieldRef(sm, units, s, (StaticFieldRef) concreteRef, context);
        else
            throw new UnknownASTNodeException();
    }/*
     * LocalOrConstant{RHSFirstContext,RHSSecondContext,IfFirstContext,IfSecondContext,CastContext,InstanceOfContext,
     * InvokeAndAssignTargetContextImpl,InvokeAndAssignArgumentContext,InvokeOnlyTargetContext,InvokeOnlyArgumentContext,
     * LengthContext,NegContext,NewMultiArrayContext,NewArrayContext,
     * RHSContext,EnterMonitorContext,ExitMonitorContext,LookupSwitchContext,TableSwitchContext,
     * ReturnContext,ThrowContext}  ::= Local | Constant
     */

    public void visitLocalOrConstant(SootMethod sm, Chain units, Stmt s, Value right, LocalOrConstantContext context) {
        if (right instanceof Local)
            nextVisitor.visitLocal(sm, units, s, (Local) right, context);
        else if (right instanceof Constant)
            nextVisitor.visitConstant(sm, units, s, (Constant) right, context);
        else
            throw new UnknownASTNodeException();
    }/*
     * Constant{{RHSFirstContext,RHSSecondContext,IfFirstContext,IfSecondContext,CastContext,InstanceOfContext,
     * InvokeAndAssignTargetContextImpl,InvokeAndAssignArgumentContext,InvokeOnlyTargetContext,InvokeOnlyArgumentContext,
     * LengthContext,NegContext,NewMultiArrayContext,NewArrayContext,
     * RHSContext,EnterMonitorContext,ExitMonitorContext,LookupSwitchContext,TableSwitchContext,
     * ReturnContext,ThrowContext}
     */

    public void visitConstant(SootMethod sm, Chain units, Stmt s, Constant constant, LocalOrConstantContext context) {

    }/*
     * Local{RHSFirstContext,RHSSecondContext,IfFirstContext,IfSecondContext,CastContext,InstanceOfContext,
     * InvokeAndAssignTargetContextImpl,InvokeAndAssignArgumentContext,InvokeOnlyTargetContext,InvokeOnlyArgumentContext,
     * LengthContext,NegContext,NewMultiArrayContext,NewArrayContext,
     * RHSContext,EnterMonitorContext,ExitMonitorContext,LookupSwitchContext,TableSwitchContext,
     * ReturnContext,ThrowContext,IdentityContext,LHSContext}
     */

    public void visitLocal(SootMethod sm, Chain units, Stmt s, Local local, LocalContext context) {

    }/*
     * StaticFieldRef{RHSContext,LHSContext}
     */

    public void visitStaticFieldRef(SootMethod sm, Chain units, Stmt s, StaticFieldRef staticFieldRef, RefContext context) {

    }/*
     * ArrayRef{RHSContext,LHSContext}
     */

    public void visitArrayRef(SootMethod sm, Chain units, Stmt s, ArrayRef arrayRef, RefContext context) {

    }/*
     * InstanceFieldRef{RHSContext,LHSContext}
     */

    public void visitInstanceFieldRef(SootMethod sm, Chain units, Stmt s, InstanceFieldRef instanceFieldRef, RefContext context) {

    }/*
     * CaughtExceptionRef{IdentityContext}
     */

    public void visitCaughtExceptionRef(SootMethod sm, Chain units, IdentityStmt s, CaughtExceptionRef caughtExceptionRef) {

    }/*
     * ParameterRef{IdentityContext}
     */

    public void visitParameterRef(SootMethod sm, Chain units, IdentityStmt s, ParameterRef parameterRef) {

    }/*
     * ThisRef{IdentityContext}
     */

    public void visitThisRef(SootMethod sm, Chain units, IdentityStmt s, ThisRef thisRef) {

    }/*
     * Binop{RHSContext,IfContext}
     */

    public void visitBinop(SootMethod sm, Chain units, Stmt s, String op, BinopExprContext context) {

    }/*
     * Signature{InvokeAndAssignContext,InvokeOnlyContext}
     */

    public void visitSignature(SootMethod sm, Chain units, Stmt s, String signature, InvokeContext context) {

    }/*
     * Label{GotoContext,IfContext,LookupSwitchContext,LookupSwitchDefaultContext,TableSwitchContext,TableSwitchDefaultContext}
     */

    public void visitLabel(SootMethod sm, Chain units, Stmt gotoStmt, Unit target, LabelContext context) {

    }
}
