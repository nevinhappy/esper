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
package com.espertech.esper.epl.datetime.dtlocal;

import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.datetime.calop.CalendarForge;
import com.espertech.esper.epl.datetime.reformatop.ReformatForge;

import java.util.List;

import static com.espertech.esper.epl.datetime.dtlocal.DTLocalUtil.getCalendarOps;

public class DTLocalZonedDateTimeOpsReformatForge extends DTLocalForgeCalopReformatBase {

    public DTLocalZonedDateTimeOpsReformatForge(List<CalendarForge> calendarForges, ReformatForge reformatForge) {
        super(calendarForges, reformatForge);
    }

    public DTLocalEvaluator getDTEvaluator() {
        return new DTLocalZonedDateTimeOpsReformatEval(getCalendarOps(calendarForges), reformatForge.getOp());
    }

    public CodegenExpression codegen(CodegenExpression inner, Class innerType, CodegenParamSetExprPremade params, CodegenContext context) {
        return DTLocalZonedDateTimeOpsReformatEval.codegen(this, inner, params, context);
    }
}
