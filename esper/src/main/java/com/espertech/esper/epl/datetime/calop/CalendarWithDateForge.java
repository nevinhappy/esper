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
package com.espertech.esper.epl.datetime.calop;

import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.codegen.model.method.CodegenParamSetExprPremade;
import com.espertech.esper.epl.expression.core.ExprForge;

public class CalendarWithDateForge implements CalendarForge {

    protected ExprForge year;
    protected ExprForge month;
    protected ExprForge day;

    public CalendarWithDateForge(ExprForge year, ExprForge month, ExprForge day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public CalendarOp getEvalOp() {
        return new CalendarWithDateForgeOp(year.getExprEvaluator(), month.getExprEvaluator(), day.getExprEvaluator());
    }

    public CodegenExpression codegenCalendar(CodegenExpression cal, CodegenParamSetExprPremade params, CodegenContext context) {
        return CalendarWithDateForgeOp.codegenCalendar(this, cal, params, context);
    }

    public CodegenExpression codegenLDT(CodegenExpression ldt, CodegenParamSetExprPremade params, CodegenContext context) {
        return CalendarWithDateForgeOp.codegenLDT(this, ldt, params, context);
    }

    public CodegenExpression codegenZDT(CodegenExpression zdt, CodegenParamSetExprPremade params, CodegenContext context) {
        return CalendarWithDateForgeOp.codegenZDT(this, zdt, params, context);
    }
}
