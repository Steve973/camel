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

import java.util.function.Supplier;

import org.apache.camel.component.dynamicrouter.control.DynamicRouterControlEndpoint.DynamicRouterControlEndpointFactory;
import org.apache.camel.component.dynamicrouter.control.DynamicRouterControlProducer.DynamicRouterControlProducerFactory;
import org.apache.camel.spi.Metadata;

/**
 * Constants pertaining to the Dynamic Router Control operations.
 */
public abstract class DynamicRouterControlConstants {

    /**
     * The camel version where the dynamic router control channel endpoint was introduced.
     */
    public static final String FIRST_VERSION_CONTROL = "4.3.0";

    /**
     * The component name/scheme for the {@link DynamicRouterControlEndpoint}.
     */
    public static final String COMPONENT_SCHEME_CONTROL = "dynamic-router-control";

    /**
     * Convenient constant for the control channel URI.
     */
    public static final String CONTROL_CHANNEL_URI = COMPONENT_SCHEME_CONTROL;

    /**
     * The title of the dynamic router control endpoint, for the auto-generated documentation.
     */
    public static final String TITLE_CONTROL = "Dynamic Router Control";

    /**
     * The syntax of the control endpoint, for the auto-generated documentation.
     */
    public static final String SYNTAX_CONTROL = COMPONENT_SCHEME_CONTROL + ":controlAction";

    /**
     * Subscribe control channel action.
     */
    public static final String CONTROL_ACTION_SUBSCRIBE = "subscribe";

    /**
     * Unsubscribe control channel action.
     */
    public static final String CONTROL_ACTION_UNSUBSCRIBE = "unsubscribe";

    /**
     * Header name for the control action.
     */
    @Metadata(description = "The control action, either 'subscribe' or 'unsubscribe'.",
              enums = CONTROL_ACTION_SUBSCRIBE + "," + CONTROL_ACTION_UNSUBSCRIBE,
              javaType = "String")
    public static final String CONTROL_ACTION = "CamelDynamicRouterControlAction";

    /**
     * Header name for the subscribe channel.
     */
    @Metadata(description = "The Dynamic Router channel that the subscriber is subscribing on.",
              javaType = "String")
    public static final String CONTROL_SUBSCRIBE_CHANNEL = "CamelDynamicRouterSubscribeChannel";

    /**
     * Header name for the subscription ID.
     */
    @Metadata(description = "The subscription ID.",
              javaType = "String")
    public static final String CONTROL_SUBSCRIPTION_ID = "CamelDynamicRouterSubscriptionId";

    /**
     * Header name for the destination URI.
     */
    @Metadata(description = "The URI on which the routing participant wants to receive matching exchanges.",
              javaType = "String")
    public static final String CONTROL_DESTINATION_URI = "CamelDynamicRouterDestinationUri";

    /**
     * Header name for the routing priority.
     */
    @Metadata(description = "The priority of this subscription",
              javaType = "String")
    public static final String CONTROL_PRIORITY = "CamelDynamicRouterPriority";

    /**
     * Header name for the predicate.
     */
    @Metadata(description = "The predicate to evaluate exchanges for this subscription",
              javaType = "String")
    public static final String CONTROL_PREDICATE = "CamelDynamicRouterPredicate";

    /**
     * Header name for the predicate bean reference.
     */
    @Metadata(description = "The name of the bean in the registry that identifies the subscription predicate.",
              javaType = "String")
    public static final String CONTROL_PREDICATE_BEAN = "CamelDynamicRouterPredicateBean";

    /**
     * Header name for the predicate expression language.
     */
    @Metadata(description = "The language for the predicate when supplied as a string.",
              javaType = "String")
    public static final String CONTROL_EXPRESSION_LANGUAGE = "CamelDynamicRouterExpressionLanguage";

    /**
     * The supplier for the control endpoint factory.
     */
    public static final Supplier<DynamicRouterControlEndpointFactory> CONTROL_ENDPOINT_FACTORY_SUPPLIER
            = DynamicRouterControlEndpointFactory::new;

    /**
     * The supplier for the producer factory.
     */
    public static final Supplier<DynamicRouterControlProducerFactory> CONTROL_PRODUCER_FACTORY_SUPPLIER
            = DynamicRouterControlProducerFactory::new;
}
