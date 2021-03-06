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
package com.espertech.esper.common.internal.context.util;

import com.espertech.esper.common.internal.event.core.MappedEventBean;

public class ContextAgentInstanceInfo {
    private final MappedEventBean contextProperties;
    private final AgentInstanceFilterProxy filterProxy;

    public ContextAgentInstanceInfo(MappedEventBean contextProperties, AgentInstanceFilterProxy filterProxy) {
        this.contextProperties = contextProperties;
        this.filterProxy = filterProxy;
    }

    public MappedEventBean getContextProperties() {
        return contextProperties;
    }

    public AgentInstanceFilterProxy getFilterProxy() {
        return filterProxy;
    }
}
