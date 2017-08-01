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
package com.espertech.esper.epl.enummethod.eval;

import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetEnumMethodNonPremade;
import com.espertech.esper.epl.expression.core.ExprForge;
import com.espertech.esper.event.arr.ObjectArrayEventType;

public class EnumMostLeastFrequentScalarLamdaForge extends EnumForgeBase {

    protected final boolean isMostFrequent;
    protected final ObjectArrayEventType resultEventType;

    public EnumMostLeastFrequentScalarLamdaForge(ExprForge innerExpression, int streamCountIncoming, boolean mostFrequent, ObjectArrayEventType resultEventType) {
        super(innerExpression, streamCountIncoming);
        isMostFrequent = mostFrequent;
        this.resultEventType = resultEventType;
    }

    public EnumEval getEnumEvaluator() {
        return new EnumMostLeastFrequentScalarLamdaForgeEval(this, innerExpression.getExprEvaluator());
    }

    public CodegenExpression codegen(CodegenParamSetEnumMethodNonPremade premade, CodegenContext context) {
        return EnumMostLeastFrequentScalarLamdaForgeEval.codegen(this, premade, context);
    }
}
