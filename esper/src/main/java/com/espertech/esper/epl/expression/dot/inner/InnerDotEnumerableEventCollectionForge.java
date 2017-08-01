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
package com.espertech.esper.epl.expression.dot.inner;

import com.espertech.esper.client.EventType;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.expression.core.ExprEnumerationForge;
import com.espertech.esper.epl.expression.dot.ExprDotEvalRootChildInnerEval;
import com.espertech.esper.epl.expression.dot.ExprDotEvalRootChildInnerForge;
import com.espertech.esper.epl.rettype.EPType;
import com.espertech.esper.epl.rettype.EPTypeHelper;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.constantNull;

public class InnerDotEnumerableEventCollectionForge implements ExprDotEvalRootChildInnerForge {

    protected final ExprEnumerationForge rootLambdaForge;
    protected final EventType eventType;

    public InnerDotEnumerableEventCollectionForge(ExprEnumerationForge rootLambdaForge, EventType eventType) {
        this.rootLambdaForge = rootLambdaForge;
        this.eventType = eventType;
    }

    public ExprDotEvalRootChildInnerEval getInnerEvaluator() {
        return new InnerDotEnumerableEventCollectionEval(rootLambdaForge.getExprEvaluatorEnumeration());
    }

    public CodegenExpression codegenEvaluate(CodegenParamSetExprPremade params, CodegenContext context) {
        return rootLambdaForge.evaluateGetROCollectionEventsCodegen(params, context);
    }

    public CodegenExpression evaluateGetROCollectionEventsCodegen(CodegenParamSetExprPremade params, CodegenContext context) {
        return rootLambdaForge.evaluateGetROCollectionEventsCodegen(params, context);
    }

    public CodegenExpression evaluateGetROCollectionScalarCodegen(CodegenParamSetExprPremade params, CodegenContext context) {
        return rootLambdaForge.evaluateGetROCollectionEventsCodegen(params, context);
    }

    public CodegenExpression evaluateGetEventBeanCodegen(CodegenParamSetExprPremade params, CodegenContext context) {
        return constantNull();
    }

    public EventType getEventTypeCollection() {
        return eventType;
    }

    public Class getComponentTypeCollection() {
        return null;
    }

    public EventType getEventTypeSingle() {
        return null;
    }

    public EPType getTypeInfo() {
        return EPTypeHelper.collectionOfEvents(eventType);
    }
}
