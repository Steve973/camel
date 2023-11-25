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
package org.apache.camel.component.dynamicrouter.control;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.component.dynamicrouter.DynamicRouterComponent;
import org.apache.camel.component.dynamicrouter.DynamicRouterProcessor;
import org.apache.camel.component.dynamicrouter.control.DynamicRouterControlMessage.SubscribeMessageBuilder;
import org.apache.camel.component.dynamicrouter.control.DynamicRouterControlMessage.UnsubscribeMessageBuilder;
import org.apache.camel.spi.Language;
import org.apache.camel.support.builder.PredicateBuilder;
import org.apache.camel.test.infra.core.CamelContextExtension;
import org.apache.camel.test.infra.core.DefaultCamelContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_ACTION;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_DESTINATION_URI;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_EXPRESSION_LANGUAGE;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_PREDICATE;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_PRIORITY;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_SUBSCRIBE_CHANNEL;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_SUBSCRIPTION_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DynamicRouterControlProducerTest {

    @RegisterExtension
    static CamelContextExtension contextExtension = new DefaultCamelContextExtension();

    @Mock
    DynamicRouterComponent routerComponent;

    @Mock
    DynamicRouterProcessor routerProcessor;

    @Mock
    DynamicRouterControlConfiguration configuration;

    @Mock
    DynamicRouterControlEndpoint endpoint;

    @Mock
    Exchange exchange;

    @Mock
    Message message;

    CamelContext context;

    DynamicRouterControlProducer producer;

    @BeforeEach
    void setup() {
        context = contextExtension.getContext();
        producer = new DynamicRouterControlProducer(endpoint, routerComponent, configuration);
        producer.setCamelContext(context);
    }

    @Test
    void obtainPredicateFromBean() {
        String beanName = "testPredicate";
        Predicate expectedPredicate = PredicateBuilder.constant(true);
        context.getRegistry().bind(beanName, Predicate.class, expectedPredicate);
        Predicate actualPredicate = producer.obtainPredicate("", "", "", beanName);
        assertEquals(expectedPredicate, actualPredicate);
    }

    @Test
    void obtainPredicateFromExpression() {
        String expressionLanguage = "simple";
        String trueExpression = "true";
        Language language = context.resolveLanguage(expressionLanguage);
        Predicate expectedPredicate = language.createPredicate(trueExpression);
        Predicate actualPredicate = producer.obtainPredicate("", expressionLanguage, trueExpression, null);
        assertEquals(expectedPredicate, actualPredicate);
    }

    @Test
    void obtainPredicateFromBody() {
        Predicate expectedPredicate = PredicateBuilder.constant(true);
        Predicate actualPredicate = producer.obtainPredicate(expectedPredicate, "", "", null);
        assertEquals(expectedPredicate, actualPredicate);
    }

    @Test
    void performSubscribeActionFromHeader() {
        String subscribeChannel = "testChannel";
        Map<String, Object> headers = Map.of(
                CONTROL_ACTION, "subscribe",
                CONTROL_SUBSCRIBE_CHANNEL, subscribeChannel,
                CONTROL_SUBSCRIPTION_ID, "testId",
                CONTROL_DESTINATION_URI, "mock://test",
                CONTROL_PREDICATE, "true",
                CONTROL_EXPRESSION_LANGUAGE, "simple",
                CONTROL_PRIORITY, 10);
        Mockito.when(message.getHeaders()).thenReturn(headers);
        Mockito.when(routerComponent.getRoutingProcessor(subscribeChannel)).thenReturn(routerProcessor);
        Mockito.doNothing().when(routerProcessor).addFilter(any(DynamicRouterControlMessage.class));
        producer.performControlAction(message);
        Mockito.verify(routerProcessor, Mockito.times(1)).addFilter(any(DynamicRouterControlMessage.class));
    }

    @Test
    void performUnsubscribeActionFromHeader() {
        String subscriptionId = "testId";
        String subscribeChannel = "testChannel";
        Map<String, Object> headers = Map.of(
                CONTROL_ACTION, "unsubscribe",
                CONTROL_SUBSCRIBE_CHANNEL, subscribeChannel,
                CONTROL_SUBSCRIPTION_ID, subscriptionId);
        Mockito.when(message.getHeaders()).thenReturn(headers);
        Mockito.when(routerComponent.getRoutingProcessor(subscribeChannel)).thenReturn(routerProcessor);
        Mockito.doNothing().when(routerProcessor).removeFilter(subscriptionId);
        producer.performControlAction(message);
        Mockito.verify(routerProcessor, Mockito.times(1)).removeFilter(subscriptionId);
    }

    @Test
    void performSubscribeActionFromBody() throws Exception {
        String subscribeChannel = "testChannel";
        Language language = context.resolveLanguage("simple");
        Predicate predicate = language.createPredicate("true");
        DynamicRouterControlMessage controlMessage = new SubscribeMessageBuilder()
                .id("testId")
                .channel("testChannel")
                .endpointUri("mock://test")
                .predicate(predicate)
                .priority(10)
                .build();
        Mockito.when(exchange.getMessage()).thenReturn(message);
        Mockito.when(message.getBody()).thenReturn(controlMessage);
        Mockito.when(routerComponent.getRoutingProcessor(subscribeChannel)).thenReturn(routerProcessor);
        Mockito.doNothing().when(routerProcessor).addFilter(controlMessage);
        producer.process(exchange);
        Mockito.verify(routerProcessor, Mockito.times(1)).addFilter(controlMessage);
    }

    @Test
    void performUnsubscribeActionFromBody() throws Exception {
        String subscriptionId = "testId";
        String subscribeChannel = "testChannel";
        DynamicRouterControlMessage controlMessage = new UnsubscribeMessageBuilder()
                .id(subscriptionId)
                .channel("testChannel")
                .build();
        Mockito.when(exchange.getMessage()).thenReturn(message);
        Mockito.when(message.getBody()).thenReturn(controlMessage);
        Mockito.when(routerComponent.getRoutingProcessor(subscribeChannel)).thenReturn(routerProcessor);
        Mockito.doNothing().when(routerProcessor).removeFilter(subscriptionId);
        producer.process(exchange);
        Mockito.verify(routerProcessor, Mockito.times(1)).removeFilter(subscriptionId);
    }

    @Test
    void processWithUnknownControlActionError() {
        Mockito.when(exchange.getMessage()).thenReturn(message);
        Mockito.when(message.getBody()).thenReturn("String is not a control message");
        assertThrows(IllegalArgumentException.class, () -> producer.process(exchange));
    }
}
