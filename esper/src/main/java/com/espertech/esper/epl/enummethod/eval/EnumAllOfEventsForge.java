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

public class EnumAllOfEventsForge extends EnumForgeBase {

    public EnumAllOfEventsForge(ExprForge innerExpression, int streamCountIncoming) {
        super(innerExpression, streamCountIncoming);
    }

    public EnumEval getEnumEvaluator() {
        return new EnumAllOfEventsForgeEval(this, innerExpression.getExprEvaluator());
    }

    public CodegenExpression codegen(CodegenParamSetEnumMethodNonPremade premade, CodegenContext context) {
        return EnumAllOfEventsForgeEval.codegen(this, premade, context);
    }
}
