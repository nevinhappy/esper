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
package com.espertech.esper.example.terminal.jse.listener;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;

public class CheckinProblemListener extends BaseTerminalListener {

    public CheckinProblemListener(ComplexEventListener complexEventListener) {
        super(complexEventListener);
    }

    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        String terminal = (String) newEvents[0].get("terminal");
        String message = "Customer checkin problem detected at terminal " + terminal;
        complexEventListener.onComplexEvent(message);
    }
}
