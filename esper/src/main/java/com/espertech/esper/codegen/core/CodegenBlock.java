/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.codegen.core;

import com.espertech.esper.codegen.model.expression.CodegenExpressionRef;
import com.espertech.esper.codegen.model.statement.CodegenStatementIf;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.statement.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

public class CodegenBlock {
    private final CodegenMethod parentMethod;
    private final CodegenStatementWBlockBase parentWBlock;
    private boolean closed;
    protected List<CodegenStatement> statements = new ArrayList<>(4);

    public CodegenBlock(CodegenMethod parentMethod) {
        this.parentMethod = parentMethod;
        this.parentWBlock = null;
    }

    public CodegenBlock(CodegenStatementWBlockBase parentWBlock) {
        this.parentWBlock = parentWBlock;
        this.parentMethod = null;
    }

    public CodegenBlock expression(CodegenExpression expression) {
        checkClosed();
        statements.add(new CodegenStatementExpression(expression));
        return this;
    }

    public CodegenBlock compoundAssignment(String ref, String op, CodegenExpression rhs) {
        checkClosed();
        statements.add(new CodegenStatementCompoundAssign(ref, op, rhs));
        return this;
    }

    public CodegenBlock ifConditionReturnConst(CodegenExpression condition, Object constant) {
        checkClosed();
        statements.add(new CodegenStatementIfConditionReturnConst(condition, constant));
        return this;
    }

    public CodegenBlock ifNotInstanceOf(String name, Class clazz) {
        return ifInstanceOf(name, clazz, true);
    }

    public CodegenBlock ifInstanceOf(String name, Class clazz) {
        return ifInstanceOf(name, clazz, false);
    }

    private CodegenBlock ifInstanceOf(String name, Class clazz, boolean not) {
        return ifCondition(!not ? instanceOf(ref(name), clazz) : notInstanceOf(ref(name), clazz));
    }

    public CodegenBlock ifRefNull(String ref) {
        return ifCondition(equalsNull(ref(ref)));
    }

    public CodegenBlock ifRefNotNull(String ref) {
        return ifCondition(notEqualsNull(ref(ref)));
    }

    public CodegenBlock ifCondition(CodegenExpression condition) {
        checkClosed();
        CodegenStatementIf builder = new CodegenStatementIf(this);
        statements.add(builder);
        return builder.ifBlock(condition);
    }

    public CodegenBlock synchronizedOn(CodegenExpressionRef ref) {
        checkClosed();
        CodegenStatementSynchronized builder = new CodegenStatementSynchronized(this, ref);
        statements.add(builder);
        return builder.makeBlock();
    }

    public CodegenBlock forLoopIntSimple(String name, CodegenExpression upperLimit) {
        checkClosed();
        CodegenStatementForIntSimple forStmt = new CodegenStatementForIntSimple(this, name, upperLimit);
        CodegenBlock block = new CodegenBlock(forStmt);
        forStmt.setBlock(block);
        statements.add(forStmt);
        return block;
    }

    public CodegenBlock forLoop(Class type, String name, CodegenExpression initialization, CodegenExpression termination, CodegenExpression increment) {
        checkClosed();
        CodegenStatementFor forStmt = new CodegenStatementFor(this, type, name, initialization, termination, increment);
        CodegenBlock block = new CodegenBlock(forStmt);
        forStmt.setBlock(block);
        statements.add(forStmt);
        return block;
    }

    public CodegenBlock forEach(Class type, String name, CodegenExpression target) {
        checkClosed();
        CodegenStatementForEach forStmt = new CodegenStatementForEach(this, type, name, target);
        CodegenBlock block = new CodegenBlock(forStmt);
        forStmt.setBlock(block);
        statements.add(forStmt);
        return block;
    }

    public CodegenBlock tryCatch() {
        checkClosed();
        CodegenStatementTryCatch tryCatch = new CodegenStatementTryCatch(this);
        CodegenBlock block = new CodegenBlock(tryCatch);
        tryCatch.setTry(block);
        statements.add(tryCatch);
        return block;
    }

    public CodegenBlock declareVarWCast(Class clazz, String var, String rhsName) {
        checkClosed();
        statements.add(new CodegenStatementDeclareVarWCast(clazz, var, rhsName));
        return this;
    }

    public CodegenBlock declareVar(Class clazz, String var, CodegenExpression initializer) {
        checkClosed();
        statements.add(new CodegenStatementDeclareVar(clazz, null, var, initializer));
        return this;
    }

    public CodegenBlock declareVar(Class clazz, Class optionalTypeVariable, String var, CodegenExpression initializer) {
        checkClosed();
        statements.add(new CodegenStatementDeclareVar(clazz, optionalTypeVariable, var, initializer));
        return this;
    }

    public CodegenBlock declareVarNoInit(Class clazz, String var) {
        checkClosed();
        statements.add(new CodegenStatementDeclareVar(clazz, null, var, null));
        return this;
    }

    public CodegenBlock declareVarNull(Class clazz, String var) {
        checkClosed();
        statements.add(new CodegenStatementDeclareVarNull(clazz, var));
        return this;
    }

    public CodegenBlock assignRef(String ref, CodegenExpression assignment) {
        checkClosed();
        statements.add(new CodegenStatementAssignRef(ref, assignment));
        return this;
    }

    public CodegenBlock breakLoop() {
        checkClosed();
        statements.add(CodegenStatementBreakLoop.INSTANCE);
        return this;
    }

    public CodegenBlock assignArrayElement(String ref, CodegenExpression index, CodegenExpression assignment) {
        return assignArrayElement(ref(ref), index, assignment);
    }

    public CodegenBlock assignArrayElement(CodegenExpression ref, CodegenExpression index, CodegenExpression assignment) {
        checkClosed();
        statements.add(new CodegenStatementAssignArrayElement(ref, index, assignment));
        return this;
    }

    public CodegenBlock exprDotMethod(CodegenExpression expression, String method, CodegenExpression ... params) {
        checkClosed();
        statements.add(new CodegenStatementExprDotMethod(expression, method, params));
        return this;
    }

    public CodegenBlock ifRefNullReturnFalse(String ref) {
        checkClosed();
        statements.add(new CodegenStatementIfRefNullReturnFalse(ref));
        return this;
    }

    public CodegenBlock ifRefNotTypeReturnConst(String ref, Class type, Object constant) {
        checkClosed();
        statements.add(new CodegenStatementIfRefNotTypeReturnConst(ref, type, constant));
        return this;
    }

    public CodegenBlock ifRefNullReturnNull(String ref) {
        checkClosed();
        statements.add(new CodegenStatementIfRefNullReturnNull(ref));
        return this;
    }

    public CodegenBlock blockReturn(CodegenExpression expression) {
        if (parentWBlock == null) {
            throw new IllegalStateException("No codeblock parent, use 'methodReturn... instead");
        }
        checkClosed();
        closed = true;
        statements.add(new CodegenStatementReturnExpression(expression));
        return parentWBlock.getParent();
    }

    public CodegenBlock blockReturnNoValue() {
        if (parentWBlock == null) {
            throw new IllegalStateException("No codeblock parent, use 'methodReturn... instead");
        }
        checkClosed();
        closed = true;
        statements.add(CodegenStatementReturnNoValue.INSTANCE);
        return parentWBlock.getParent();
    }

    public CodegenStatementTryCatch tryReturn(CodegenExpression expression) {
        if (parentWBlock == null) {
            throw new IllegalStateException("No codeblock parent, use 'methodReturn... instead");
        }
        if (!(parentWBlock instanceof CodegenStatementTryCatch)) {
            throw new IllegalStateException("Codeblock parent is not try-catch");
        }
        checkClosed();
        closed = true;
        statements.add(new CodegenStatementReturnExpression(expression));
        return (CodegenStatementTryCatch) parentWBlock;
    }

    public CodegenStatementTryCatch tryEnd() {
        if (parentWBlock == null) {
            throw new IllegalStateException("No codeblock parent, use 'methodReturn... instead");
        }
        if (!(parentWBlock instanceof CodegenStatementTryCatch)) {
            throw new IllegalStateException("Codeblock parent is not try-catch");
        }
        closed = true;
        return (CodegenStatementTryCatch) parentWBlock;
    }

    public CodegenBlock blockThrow(CodegenExpression expression) {
        if (parentWBlock == null) {
            throw new IllegalStateException("No codeblock parent, use 'methodReturn... instead");
        }
        checkClosed();
        closed = true;
        statements.add(new CodegenStatementThrow(expression));
        return parentWBlock.getParent();
    }

    public CodegenBlock blockEnd() {
        if (parentWBlock == null) {
            throw new IllegalStateException("No codeblock parent, use 'methodReturn... instead");
        }
        checkClosed();
        closed = true;
        return parentWBlock.getParent();
    }

    public String methodReturn(CodegenExpression expression) {
        if (parentMethod == null) {
            throw new IllegalStateException("No method parent, use 'blockReturn... instead");
        }
        checkClosed();
        closed = true;
        statements.add(new CodegenStatementReturnExpression(expression));
        return parentMethod.getFootprint().getMethodName();
    }

    public String methodEnd() {
        if (parentMethod == null) {
            throw new IllegalStateException("No method parent, use 'blockReturn... instead");
        }
        checkClosed();
        closed = true;
        return parentMethod.getFootprint().getMethodName();
    }

    public void render(StringBuilder builder, Map<Class, String> imports, int level, CodegenIndent indent) {
        for (CodegenStatement statement : statements) {
            indent.indent(builder, level);
            statement.render(builder, imports, level, indent);
        }
    }

    public void mergeClasses(Set<Class> classes) {
        for (CodegenStatement statement : statements) {
            statement.mergeClasses(classes);
        }
    }

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("Code block already closed");
        }
    }

    public CodegenBlock ifElseIf(CodegenExpression condition) {
        checkClosed();
        closed = true;
        if (parentMethod != null) {
            throw new IllegalStateException("If-block-end in method?");
        }
        if (!(parentWBlock instanceof CodegenStatementIf)) {
            throw new IllegalStateException("If-block-end in method?");
        }
        CodegenStatementIf ifBuilder = (CodegenStatementIf) parentWBlock;
        return ifBuilder.addElseIf(condition);
    }

    public CodegenBlock ifElse() {
        closed = true;
        if (parentMethod != null) {
            throw new IllegalStateException("If-block-end in method?");
        }
        if (!(parentWBlock instanceof CodegenStatementIf)) {
            throw new IllegalStateException("If-block-end in method?");
        }
        CodegenStatementIf ifBuilder = (CodegenStatementIf) parentWBlock;
        return ifBuilder.addElse();
    }

    public void ifReturn(CodegenExpression result) {
        checkClosed();
        closed = true;
        if (parentMethod != null) {
            throw new IllegalStateException("If-block-end in method?");
        }
        if (!(parentWBlock instanceof CodegenStatementIf)) {
            throw new IllegalStateException("If-block-end in method?");
        }
        statements.add(new CodegenStatementReturnExpression(result));
    }

    public CodegenBlock blockContinue() {
        checkClosed();
        closed = true;
        if (parentMethod != null) {
            throw new IllegalStateException("If-block-end in method?");
        }
        statements.add(CodegenStatementContinue.INSTANCE);
        return parentWBlock.getParent();
    }
}
