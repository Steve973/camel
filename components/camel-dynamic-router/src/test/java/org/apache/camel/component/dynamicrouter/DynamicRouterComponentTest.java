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

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.component.dynamicrouter.DynamicRouterEndpoint.DynamicRouterEndpointFactory;
import org.apache.camel.component.dynamicrouter.DynamicRouterProcessor.DynamicRouterMulticastProcessorFactory;
import org.apache.camel.component.dynamicrouter.DynamicRouterProducer.DynamicRouterProducerFactory;
import org.apache.camel.component.dynamicrouter.PrioritizedFilter.PrioritizedFilterFactory;
import org.apache.camel.spi.ProducerCache;
import org.apache.camel.test.infra.core.CamelContextExtension;
import org.apache.camel.test.infra.core.DefaultCamelContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DynamicRouterComponentTest {

    static final String DYNAMIC_ROUTER_CHANNEL = "test";

    @RegisterExtension
    static CamelContextExtension contextExtension = new DefaultCamelContextExtension();

    @Mock
    protected DynamicRouterProducer producer;

    @Mock
    DynamicRouterEndpoint endpoint;

    @Mock
    DynamicRouterProcessor processor;

    @Mock
    PrioritizedFilter prioritizedFilter;

    DynamicRouterComponent component;

    CamelContext context;

    DynamicRouterEndpointFactory endpointFactory;

    DynamicRouterMulticastProcessorFactory processorFactory;

    DynamicRouterProducerFactory producerFactory;

    PrioritizedFilterFactory prioritizedFilterFactory;

    @BeforeEach
    void setup() {
        context = contextExtension.getContext();
        endpointFactory = new DynamicRouterEndpointFactory() {
            @Override
            public DynamicRouterEndpoint getInstance(
                    String uri, DynamicRouterComponent component, DynamicRouterConfiguration configuration,
                    Supplier<DynamicRouterMulticastProcessorFactory> processorFactorySupplier,
                    Supplier<DynamicRouterProducerFactory> producerFactorySupplier,
                    Supplier<PrioritizedFilterFactory> filterProcessorFactorySupplier) {
                return endpoint;
            }
        };
        processorFactory = new DynamicRouterMulticastProcessorFactory() {
            @Override
            public DynamicRouterProcessor getInstance(
                    String id, CamelContext camelContext, Route route, String recipientMode, boolean warnDroppedMessage,
                    Supplier<PrioritizedFilterFactory> filterProcessorFactorySupplier, ProducerCache producerCache,
                    AggregationStrategy aggregationStrategy, boolean parallelProcessing,
                    ExecutorService executorService, boolean shutdownExecutorService, boolean streaming,
                    boolean stopOnException, long timeout, Processor onPrepare, boolean shareUnitOfWork,
                    boolean parallelAggregate) {
                return processor;
            }
        };
        producerFactory = new DynamicRouterProducerFactory() {
            @Override
            public DynamicRouterProducer getInstance(DynamicRouterEndpoint endpoint) {
                return producer;
            }
        };
        prioritizedFilterFactory = new PrioritizedFilterFactory() {
            @Override
            public PrioritizedFilter getInstance(String id, int priority, Predicate predicate, String endpoint) {
                return prioritizedFilter;
            }
        };
        component = new DynamicRouterComponent(
                () -> endpointFactory, () -> processorFactory,
                () -> producerFactory, () -> prioritizedFilterFactory);
    }

    @Test
    void testCreateEndpoint() throws Exception {
        component.setCamelContext(context);
        Endpoint actualEndpoint = component.createEndpoint("dynamic-router:testname", "remaining", Collections.emptyMap());
        assertEquals(endpoint, actualEndpoint);
    }

    @Test
    void testCreateEndpointWithEmptyRemainingError() {
        component.setCamelContext(context);
        assertThrows(IllegalArgumentException.class,
                () -> component.createEndpoint("dynamic-router:testname", "", Collections.emptyMap()));
    }

    @Test
    void testAddRoutingProcessor() {
        component.addRoutingProcessor(DYNAMIC_ROUTER_CHANNEL, processor);
        assertEquals(processor, component.getRoutingProcessor(DYNAMIC_ROUTER_CHANNEL));
    }

    @Test
    void testAddRoutingProcessorWithSecondProcessorForChannelError() {
        component.addRoutingProcessor(DYNAMIC_ROUTER_CHANNEL, processor);
        assertEquals(processor, component.getRoutingProcessor(DYNAMIC_ROUTER_CHANNEL));
        assertThrows(IllegalArgumentException.class, () -> component.addRoutingProcessor(DYNAMIC_ROUTER_CHANNEL, processor));

    }
}
