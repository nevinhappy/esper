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
package com.espertech.esper.supportregression.client;

import com.espertech.esper.epl.agg.access.AggregationState;
import com.espertech.esper.epl.expression.core.ExprEvaluator;
import com.espertech.esper.epl.expression.core.ExprForge;
import com.espertech.esper.plugin.PlugInAggregationMultiFunctionStateContext;
import com.espertech.esper.plugin.PlugInAggregationMultiFunctionStateFactory;

public class SupportAggMFStateArrayCollScalarFactory implements PlugInAggregationMultiFunctionStateFactory {
    private final ExprForge forge;
    private final ExprEvaluator evaluator;

    public SupportAggMFStateArrayCollScalarFactory(ExprForge forge) {
        this.forge = forge;
        this.evaluator = forge.getExprEvaluator();
    }

    public AggregationState makeAggregationState(PlugInAggregationMultiFunctionStateContext stateContext) {
        return new SupportAggMFStateArrayCollScalar(this);
    }

    public ExprForge getForge() {
        return forge;
    }

    public ExprEvaluator getEvaluator() {
        return evaluator;
    }
}
