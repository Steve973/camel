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

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.CamelContext;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.Route;
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

import static org.apache.camel.component.dynamicrouter.DynamicRouterConstants.COMPONENT_SCHEME_ROUTING;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DynamicRouterEndpointTest {

    public static final String DYNAMIC_ROUTER_CHANNEL = "test";

    public static final String BASE_URI = String.format("%s:%s", COMPONENT_SCHEME_ROUTING, DYNAMIC_ROUTER_CHANNEL);

    @RegisterExtension
    static CamelContextExtension contextExtension = new DefaultCamelContextExtension();

    @Mock
    protected DynamicRouterConfiguration configuration;

    @Mock
    protected DynamicRouterProducer producer;

    @Mock
    DynamicRouterComponent component;

    @Mock
    DynamicRouterProcessor processor;

    @Mock
    PrioritizedFilter prioritizedFilter;

    DynamicRouterEndpoint endpoint;

    CamelContext context;

    DynamicRouterMulticastProcessorFactory processorFactory;

    DynamicRouterProducerFactory producerFactory;

    PrioritizedFilterFactory prioritizedFilterFactory;

    @BeforeEach
    void setup() {
        context = contextExtension.getContext();
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
        endpoint = new DynamicRouterEndpoint(
                BASE_URI, component, configuration, () -> processorFactory, () -> producerFactory,
                () -> prioritizedFilterFactory);
    }

    @Test
    void testCreateProducer() {
        Producer actualProducer = endpoint.createProducer();
        assertEquals(producer, actualProducer);
    }

    @Test
    void testCreateConsumerException() {
        assertThrows(IllegalStateException.class, () -> endpoint.createConsumer(processor));
    }
}
