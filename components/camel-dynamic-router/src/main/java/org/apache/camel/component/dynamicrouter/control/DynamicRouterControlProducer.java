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
import java.util.Optional;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.component.dynamicrouter.DynamicRouterComponent;
import org.apache.camel.component.dynamicrouter.DynamicRouterEndpoint;
import org.apache.camel.component.dynamicrouter.control.DynamicRouterControlMessage.ControlMessageType;
import org.apache.camel.component.dynamicrouter.control.DynamicRouterControlMessage.SubscribeMessageBuilder;
import org.apache.camel.spi.InvokeOnHeader;
import org.apache.camel.spi.Language;
import org.apache.camel.support.CamelContextHelper;
import org.apache.camel.support.HeaderSelectorProducer;

import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_ACTION;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_ACTION_SUBSCRIBE;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_ACTION_UNSUBSCRIBE;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_DESTINATION_URI;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_EXPRESSION_LANGUAGE;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_PREDICATE;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_PREDICATE_BEAN;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_PRIORITY;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_SUBSCRIBE_CHANNEL;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_SUBSCRIPTION_ID;

/**
 * A {@link org.apache.camel.Producer} implementation to process control channel messages for the Dynamic Router.
 */
public class DynamicRouterControlProducer extends HeaderSelectorProducer {

    private final DynamicRouterComponent routerComponent;

    /**
     * The configuration for the Dynamic Router.
     */
    private final DynamicRouterControlConfiguration configuration;

    /**
     * Create the {@link org.apache.camel.Producer} for the Dynamic Router with the supplied {@link Endpoint} URI.
     *
     * @param endpoint the {@link DynamicRouterEndpoint}
     */
    public DynamicRouterControlProducer(final DynamicRouterControlEndpoint endpoint,
                                        final DynamicRouterComponent routerComponent,
                                        final DynamicRouterControlConfiguration configuration) {
        super(endpoint, CONTROL_ACTION, configuration::getControlAction);
        this.routerComponent = routerComponent;
        this.configuration = configuration;
    }

    private Predicate obtainPredicateFromMessageBody(final Object body) {
        if (Predicate.class.isAssignableFrom(body.getClass())) {
            return (Predicate) body;
        } else {
            throw new IllegalArgumentException(
                    "To supply a predicate in the message body, the body's class must be resolvable to org.apache.camel.Predicate");
        }
    }

    private Predicate obtainPredicateFromExpression(final String predExpression, final String expressionLanguage) {
        try {
            Language language = getCamelContext().resolveLanguage(expressionLanguage);
            return language.createPredicate(predExpression);
        } catch (Exception e) {
            String message = String.format(
                    "Language '%s' and predicate expression '%s' could not create a valid predicate",
                    expressionLanguage, predExpression);
            throw new IllegalArgumentException(message, e);
        }
    }

    private Predicate obtainPredicateFromBean(final String predicateBeanName) {
        return Optional.ofNullable(CamelContextHelper.lookup(getCamelContext(), predicateBeanName, Predicate.class))
                .orElseThrow(() -> new IllegalStateException("Predicate bean could not be found"));
    }

    /**
     * Tries to obtain the subscription {@link Predicate}. First, it looks for an expression from the URI parameters. If
     * that is not available, it checks the message body for the {@link Predicate}.
     *
     * @param  body body to inspect for a {@link Predicate} if the URI parameters does not have an expression
     * @return      the {@link Predicate}
     */
    Predicate obtainPredicate(
            final Object body, final String expressionLanguage, final String predExpression, String predicateBeanName) {
        Predicate predicate;
        if (predicateBeanName != null && !predicateBeanName.isEmpty()) {
            predicate = obtainPredicateFromBean(predicateBeanName);
        } else if (predExpression != null && !predExpression.isEmpty()
                && expressionLanguage != null && !expressionLanguage.isEmpty()) {
            predicate = obtainPredicateFromExpression(predExpression, expressionLanguage);
        } else {
            predicate = obtainPredicateFromMessageBody(body);
        }
        return predicate;
    }

    @InvokeOnHeader(CONTROL_ACTION)
    public void performControlAction(final Message message) {
        Map<String, Object> headers = message.getHeaders();
        String controlAction = (String) headers.getOrDefault(CONTROL_ACTION, configuration.getControlAction());
        String subscriptionId = (String) headers.getOrDefault(CONTROL_SUBSCRIPTION_ID, configuration.getSubscriptionId());
        String subscribeChannel = (String) headers.getOrDefault(CONTROL_SUBSCRIBE_CHANNEL, configuration.getSubscribeChannel());
        switch (controlAction) {
            case CONTROL_ACTION_SUBSCRIBE:
                String destinationUri
                        = (String) headers.getOrDefault(CONTROL_DESTINATION_URI, configuration.getDestinationUri());
                String priority = String.valueOf(headers.getOrDefault(CONTROL_PRIORITY, configuration.getPriority()));
                String predicate = (String) headers.getOrDefault(CONTROL_PREDICATE, configuration.getPredicate());
                String predicateBean = (String) headers.getOrDefault(CONTROL_PREDICATE_BEAN, configuration.getPredicateBean());
                String expressionLanguage
                        = (String) headers.getOrDefault(CONTROL_EXPRESSION_LANGUAGE, configuration.getExpressionLanguage());
                DynamicRouterControlMessage subMsg = new SubscribeMessageBuilder()
                        .id(subscriptionId)
                        .channel(subscribeChannel)
                        .endpointUri(destinationUri)
                        .priority(Integer.parseInt(priority))
                        .predicate(obtainPredicate(message.getBody(), expressionLanguage, predicate, predicateBean))
                        .build();
                routerComponent.addFilter(subMsg);
                break;
            case CONTROL_ACTION_UNSUBSCRIBE:
                routerComponent.removeFilter(subscriptionId);
                break;
        }
    }

    /**
     * Process the exchange.
     *
     * @param  exchange  the exchange to process
     * @throws Exception if the consumer has a problem
     */
    @Override
    public void process(final Exchange exchange) throws Exception {
        Message message = exchange.getMessage();
        Object body = message.getBody();
        DynamicRouterControlMessage controlMessage;
        if (DynamicRouterControlMessage.class.isAssignableFrom(body.getClass())) {
            controlMessage = (DynamicRouterControlMessage) body;
            String subscribeId = controlMessage.id();
            ControlMessageType controlAction = controlMessage.messageType();
            switch (controlAction) {
                case SUBSCRIBE -> routerComponent.addFilter(controlMessage);
                case UNSUBSCRIBE -> routerComponent.removeFilter(subscribeId);
                default -> throw new IllegalArgumentException("Control action invalid: " + controlAction);
            }
        } else {
            throw new IllegalArgumentException("Could not create or find a control channel message");
        }

    }

    /**
     * Process the exchange, and use the {@link AsyncCallback} to signal completion.
     *
     * @param  exchange the exchange to process
     * @param  callback the {@link AsyncCallback} to signal when asynchronous processing has completed
     * @return          true to continue to execute synchronously, or false to continue to execute asynchronously
     */
    @Override
    public boolean process(final Exchange exchange, final AsyncCallback callback) {
        try {
            // control channel is synchronous
            process(exchange);
        } catch (Exception e) {
            exchange.setException(e);
        } finally {
            callback.done(true);
        }
        return true;
    }

    /**
     * Create a {@link DynamicRouterControlProducer} instance.
     */
    public static class DynamicRouterControlProducerFactory {

        /**
         * Create the {@link org.apache.camel.Producer} for the Dynamic Router with the supplied {@link Endpoint} URI.
         *
         * @param endpoint the {@link DynamicRouterEndpoint}
         */
        public DynamicRouterControlProducer getInstance(
                final DynamicRouterControlEndpoint endpoint,
                final DynamicRouterComponent routerComponent,
                final DynamicRouterControlConfiguration configuration) {
            return new DynamicRouterControlProducer(endpoint, routerComponent, configuration);
        }
    }
}
