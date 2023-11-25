/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.dynamicrouter;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.component.dynamicrouter.PrioritizedFilter.PrioritizedFilterFactory;
import org.apache.camel.component.dynamicrouter.control.DynamicRouterControlMessage;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;
import org.apache.camel.spi.ProducerCache;
import org.apache.camel.test.infra.core.CamelContextExtension;
import org.apache.camel.test.infra.core.DefaultCamelContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.apache.camel.component.dynamicrouter.DynamicRouterConstants.MODE_ALL_MATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DynamicRouterProcessorTest {

    static final String PROCESSOR_ID = "testProcessorId";

    static final String TEST_ID = "testId";

    @RegisterExtension
    static CamelContextExtension contextExtension = new DefaultCamelContextExtension();

    @Mock
    AsyncCallback asyncCallback;

    CamelContext context;

    DynamicRouterProcessor processor;

    @Mock
    PrioritizedFilter prioritizedFilter;

    @Mock
    ProducerCache producerCache;

    @Mock
    ExecutorService executorService;

    @Mock
    DynamicRouterControlMessage controlMessage;

    @Mock
    Predicate predicate;

    @Mock
    Exchange exchange;

    PrioritizedFilterFactory prioritizedFilterFactory;

    @BeforeEach
    void localSetup() throws Exception {
        context = contextExtension.getContext();
        prioritizedFilterFactory = new PrioritizedFilterFactory() {
            @Override
            public PrioritizedFilter getInstance(String id, int priority, Predicate predicate, String endpoint) {
                return prioritizedFilter;
            }
        };
        processor = new DynamicRouterProcessor(
                "testProcessorId", context, null, MODE_ALL_MATCH, false,
                () -> prioritizedFilterFactory, producerCache,
                new UseLatestAggregationStrategy(), false, executorService, false,
                false, false, -1, exchange -> {
                }, false, false);
        processor.doInit();
    }

    @Test
    void createFilter() {
        Mockito.when(controlMessage.priority()).thenReturn(1);
        Mockito.when(controlMessage.predicate()).thenReturn(e -> true);
        PrioritizedFilter result = processor.createFilter(controlMessage);
        assertEquals(prioritizedFilter, result);
    }

    @Test
    void addFilterAsControlMessage() {
        Mockito.when(prioritizedFilter.id()).thenReturn(TEST_ID);
        processor.addFilter(controlMessage);
        assertNotNull(processor.getFilter(TEST_ID));
    }

    @Test
    void addFilterAsFilterProcessor() {
        Mockito.when(prioritizedFilter.id()).thenReturn(TEST_ID);
        processor.addFilter(prioritizedFilter);
        PrioritizedFilter result = processor.getFilter(TEST_ID);
        assertEquals(prioritizedFilter, result);
    }

    @Test
    void addMultipleFiltersWithSameId() {
        Mockito.when(prioritizedFilter.id()).thenReturn(TEST_ID);
        Mockito.when(prioritizedFilter.predicate()).thenReturn(predicate);
        processor.addFilter(prioritizedFilter);
        processor.addFilter(prioritizedFilter);
        processor.addFilter(prioritizedFilter);
        processor.addFilter(prioritizedFilter);
        Mockito.when(predicate.matches(any(Exchange.class))).thenReturn(true);
        List<PrioritizedFilter> matchingFilters = processor.matchFilters(exchange);
        assertEquals(1, matchingFilters.size());
    }

    @Test
    void testMultipleFilterOrderByPriorityNotIdKey() {
        Mockito.when(predicate.matches(any(Exchange.class))).thenReturn(true);
        Mockito.when(prioritizedFilter.id()).thenReturn("anIdThatComesLexicallyBeforeTestId");
        Mockito.when(prioritizedFilter.predicate()).thenReturn(predicate);
        processor.addFilter(prioritizedFilter);
        addFilterAsFilterProcessor();
        List<PrioritizedFilter> matchingFilters = processor.matchFilters(exchange);
        assertEquals(2, matchingFilters.size());
        PrioritizedFilter matchingFilter = matchingFilters.get(0);
        assertEquals(TEST_ID, matchingFilter.id());
    }

    @Test
    void removeFilter() {
        addFilterAsFilterProcessor();
        processor.removeFilter(TEST_ID);
        PrioritizedFilter result = processor.getFilter(TEST_ID);
        assertNull(result);
    }

    @Test
    void matchFiltersMatches() {
        addFilterAsFilterProcessor();
        Mockito.when(prioritizedFilter.predicate()).thenReturn(predicate);
        Mockito.when(predicate.matches(any(Exchange.class))).thenReturn(true);
        PrioritizedFilter result = processor.matchFilters(exchange).get(0);
        assertEquals(TEST_ID, result.id());
    }

    @Test
    void matchFiltersDoesNotMatch() {
        addFilterAsFilterProcessor();
        Mockito.when(prioritizedFilter.predicate()).thenReturn(predicate);
        Mockito.when(predicate.matches(any(Exchange.class))).thenReturn(false);
        assertTrue(processor.matchFilters(exchange).isEmpty());
    }

    @Test
    void processMatching() {
        addFilterAsFilterProcessor();
        Mockito.when(prioritizedFilter.predicate()).thenReturn(predicate);
        Mockito.when(predicate.matches(any(Exchange.class))).thenReturn(true);
        assertTrue(processor.process(exchange, asyncCallback));
    }

    @Test
    void processNotMatching() {
        addFilterAsFilterProcessor();
        Mockito.when(prioritizedFilter.predicate()).thenReturn(predicate);
        Mockito.when(predicate.matches(any(Exchange.class))).thenReturn(false);
        assertTrue(processor.process(exchange, asyncCallback));
    }

    @Test
    void testStringIsId() {
        assertEquals(PROCESSOR_ID, processor.toString());
    }

    @Test
    void testTraceLabelIsId() {
        assertEquals(PROCESSOR_ID, processor.getTraceLabel());
    }
}
