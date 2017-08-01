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
package com.espertech.esper.epl.expression.dot;

import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.expression.core.ExprEvaluator;
import com.espertech.esper.epl.expression.core.ExprForge;
import com.espertech.esper.epl.expression.core.ExprForgeComplexityEnum;
import com.espertech.esper.epl.join.plan.FilterExprAnalyzerAffector;

public class ExprDotNodeForgeTransposeAsStream extends ExprDotNodeForge {

    private final ExprDotNodeImpl parent;
    protected final ExprForge inner;

    public ExprDotNodeForgeTransposeAsStream(ExprDotNodeImpl parent, ExprForge inner) {
        this.parent = parent;
        this.inner = inner;
    }

    public ExprEvaluator getExprEvaluator() {
        return new ExprDotNodeForgeTransposeAsStreamEval(this, inner.getExprEvaluator());
    }

    public CodegenExpression evaluateCodegen(CodegenParamSetExprPremade params, CodegenContext context) {
        return ExprDotNodeForgeTransposeAsStreamEval.codegen(this, params, context);
    }

    public ExprForgeComplexityEnum getComplexity() {
        return ExprForgeComplexityEnum.INTER;
    }

    public Class getEvaluationType() {
        return inner.getEvaluationType();
    }

    public boolean isReturnsConstantResult() {
        return false;
    }

    public FilterExprAnalyzerAffector getFilterExprAnalyzerAffector() {
        return null;
    }

    public Integer getStreamNumReferenced() {
        return null;
    }

    public String getRootPropertyName() {
        return null;
    }

    public ExprDotNodeImpl getForgeRenderable() {
        return parent;
    }
}
