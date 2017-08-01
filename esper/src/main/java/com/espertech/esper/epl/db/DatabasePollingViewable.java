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
package com.espertech.esper.epl.db;

import com.espertech.esper.client.ConfigurationInformation;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.IterablesArrayIterator;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.epl.core.EngineImportService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.expression.core.*;
import com.espertech.esper.epl.expression.visitor.ExprNodeIdentifierCollectVisitor;
import com.espertech.esper.epl.join.pollindex.PollResultIndexingStrategy;
import com.espertech.esper.epl.join.table.EventTable;
import com.espertech.esper.epl.join.table.UnindexedEventTableList;
import com.espertech.esper.epl.table.mgmt.TableService;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.schedule.SchedulingService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.view.HistoricalEventViewable;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;

import java.util.*;

/**
 * Implements a poller viewable that uses a polling strategy, a cache and
 * some input parameters extracted from event streams to perform the polling.
 */
public class DatabasePollingViewable implements HistoricalEventViewable {
    private final int myStreamNumber;
    private final PollExecStrategy pollExecStrategy;
    private final List<String> inputParameters;
    private final DataCache dataCache;
    private final EventType eventType;
    private final ThreadLocal<DataCache> dataCacheThreadLocal = new ThreadLocal<DataCache>();

    private ExprEvaluator[] evaluators;
    private SortedSet<Integer> subordinateStreams;
    private ExprEvaluatorContext exprEvaluatorContext;
    private StatementContext statementContext;

    private static final EventBean[][] NULL_ROWS;

    static {
        NULL_ROWS = new EventBean[1][];
        NULL_ROWS[0] = new EventBean[1];
    }

    private static final PollResultIndexingStrategy ITERATOR_INDEXING_STRATEGY = new PollResultIndexingStrategy() {
        public EventTable[] index(List<EventBean> pollResult, boolean isActiveCache, StatementContext statementContext) {
            return new EventTable[]{new UnindexedEventTableList(pollResult, -1)};
        }

        public String toQueryPlan() {
            return this.getClass().getSimpleName() + " unindexed";
        }
    };

    /**
     * Ctor.
     *
     * @param myStreamNumber   is the stream number of the view
     * @param inputParameters  are the event property names providing input parameter keys
     * @param pollExecStrategy is the strategy to use for retrieving results
     * @param dataCache        is looked up before using the strategy
     * @param eventType        is the type of events generated by the view
     */
    public DatabasePollingViewable(int myStreamNumber,
                                   List<String> inputParameters,
                                   PollExecStrategy pollExecStrategy,
                                   DataCache dataCache,
                                   EventType eventType) {
        this.myStreamNumber = myStreamNumber;
        this.inputParameters = inputParameters;
        this.pollExecStrategy = pollExecStrategy;
        this.dataCache = dataCache;
        this.eventType = eventType;
    }

    public void stop() {
        pollExecStrategy.destroy();
        dataCache.destroy();
    }

    public DataCache getOptionalDataCache() {
        return dataCache;
    }

    public void validate(EngineImportService engineImportService,
                         StreamTypeService streamTypeService,
                         TimeProvider timeProvider,
                         VariableService variableService,
                         TableService tableService,
                         ExprEvaluatorContext exprEvaluatorContext,
                         ConfigurationInformation configSnapshot,
                         SchedulingService schedulingService,
                         String engineURI,
                         Map<Integer, List<ExprNode>> sqlParameters,
                         EventAdapterService eventAdapterService,
                         StatementContext statementContext) throws ExprValidationException {
        this.statementContext = statementContext;
        evaluators = new ExprEvaluator[inputParameters.size()];
        subordinateStreams = new TreeSet<Integer>();
        this.exprEvaluatorContext = exprEvaluatorContext;

        int count = 0;
        ExprValidationContext validationContext = new ExprValidationContext(streamTypeService, engineImportService, statementContext.getStatementExtensionServicesContext(), null, timeProvider, variableService, tableService, exprEvaluatorContext, eventAdapterService, statementContext.getStatementName(), statementContext.getStatementId(), statementContext.getAnnotations(), null, false, false, true, false, null, false);
        for (String inputParam : inputParameters) {
            ExprNode raw = findSQLExpressionNode(myStreamNumber, count, sqlParameters);
            if (raw == null) {
                throw new ExprValidationException("Internal error find expression for historical stream parameter " + count + " stream " + myStreamNumber);
            }
            ExprNode evaluator = ExprNodeUtility.getValidatedSubtree(ExprNodeOrigin.DATABASEPOLL, raw, validationContext);
            evaluators[count++] = ExprNodeCompiler.allocateEvaluator(evaluator.getForge(), engineImportService, this.getClass(), false, statementContext.getStatementName());

            ExprNodeIdentifierCollectVisitor visitor = new ExprNodeIdentifierCollectVisitor();
            visitor.visit(evaluator);
            for (ExprIdentNode identNode : visitor.getExprProperties()) {
                if (identNode.getStreamId() == myStreamNumber) {
                    throw new ExprValidationException("Invalid expression '" + inputParam + "' resolves to the historical data itself");
                }
                subordinateStreams.add(identNode.getStreamId());
            }
        }
    }

    public EventTable[][] poll(EventBean[][] lookupEventsPerStream, PollResultIndexingStrategy indexingStrategy, ExprEvaluatorContext exprEvaluatorContext) {
        DataCache localDataCache = dataCacheThreadLocal.get();
        boolean strategyStarted = false;

        EventTable[][] resultPerInputRow = new EventTable[lookupEventsPerStream.length][];

        // Get input parameters for each row
        EventBean[] eventsPerStream;
        for (int row = 0; row < lookupEventsPerStream.length; row++) {
            Object[] lookupValues = new Object[inputParameters.size()];

            // Build lookup keys
            for (int valueNum = 0; valueNum < inputParameters.size(); valueNum++) {
                eventsPerStream = lookupEventsPerStream[row];
                Object lookupValue = evaluators[valueNum].evaluate(eventsPerStream, true, exprEvaluatorContext);
                lookupValues[valueNum] = lookupValue;
            }

            EventTable[] result = null;

            // try the threadlocal iteration cache, if set
            if (localDataCache != null) {
                EventTable[] tables = localDataCache.getCached(lookupValues, lookupValues.length);
                result = tables;
            }

            // try the connection cache
            if (result == null) {
                EventTable[] multi = dataCache.getCached(lookupValues, lookupValues.length);
                if (multi != null) {
                    result = multi;
                    if (localDataCache != null) {
                        localDataCache.put(lookupValues, lookupValues.length, multi);
                    }
                }
            }

            // use the result from cache
            if (result != null) {
                // found in cache
                resultPerInputRow[row] = result;
            } else {
                // not found in cache, get from actual polling (db query)
                try {
                    if (!strategyStarted) {
                        pollExecStrategy.start();
                        strategyStarted = true;
                    }

                    // Poll using the polling execution strategy and lookup values
                    List<EventBean> pollResult = pollExecStrategy.poll(lookupValues, exprEvaluatorContext);

                    // index the result, if required, using an indexing strategy
                    EventTable[] indexTable = indexingStrategy.index(pollResult, dataCache.isActive(), statementContext);

                    // assign to row
                    resultPerInputRow[row] = indexTable;

                    // save in cache
                    dataCache.put(lookupValues, lookupValues.length, indexTable);

                    if (localDataCache != null) {
                        localDataCache.put(lookupValues, lookupValues.length, indexTable);
                    }
                } catch (EPException ex) {
                    if (strategyStarted) {
                        pollExecStrategy.done();
                    }
                    throw ex;
                }
            }
        }

        if (strategyStarted) {
            pollExecStrategy.done();
        }

        return resultPerInputRow;
    }

    public View addView(View view) {
        view.setParent(this);
        return view;
    }

    public View[] getViews() {
        return ViewSupport.EMPTY_VIEW_ARRAY;
    }

    public boolean removeView(View view) {
        throw new UnsupportedOperationException("Subviews not supported");
    }

    public boolean hasViews() {
        return false;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Iterator<EventBean> iterator() {
        EventTable[][] tablesPerRow = poll(NULL_ROWS, ITERATOR_INDEXING_STRATEGY, exprEvaluatorContext);
        return new IterablesArrayIterator(tablesPerRow);
    }

    public SortedSet<Integer> getRequiredStreams() {
        return subordinateStreams;
    }

    public boolean hasRequiredStreams() {
        return !subordinateStreams.isEmpty();
    }

    public ThreadLocal<DataCache> getDataCacheThreadLocal() {
        return dataCacheThreadLocal;
    }

    public void removeAllViews() {
        throw new UnsupportedOperationException("Subviews not supported");
    }

    private static ExprNode findSQLExpressionNode(int myStreamNumber, int count, Map<Integer, List<ExprNode>> sqlParameters) {
        if ((sqlParameters == null) || (sqlParameters.isEmpty())) {
            return null;
        }
        List<ExprNode> parameters = sqlParameters.get(myStreamNumber);
        if ((parameters == null) || (parameters.isEmpty()) || (parameters.size() < (count + 1))) {
            return null;
        }
        return parameters.get(count);
    }
}
