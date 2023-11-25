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

import org.apache.camel.Predicate;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;

@UriParams
public class DynamicRouterControlConfiguration implements Cloneable {

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * action (subscribe or unsubscribe) by using this URI path variable.
     */
    @UriParam(label = "control", enums = "subscribe,unsubscribe",
              description = "Control channel action: subscribe or unsubscribe")
    private String controlAction;

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * subscribe channel by using this URI path variable.
     */
    @UriParam(label = "control", description = "The channel to subscribe to")
    private String subscribeChannel;

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * subscription ID by using this URI param. If one is not supplied, one will be generated and returned.
     */
    @UriParam(label = "control", description = "The subscription ID; if unspecified, one will be assigned and returned.")
    private String subscriptionId;

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * destination URI by using this URI param.
     */
    @UriParam(label = "control", description = "The destination URI for exchanges that match.")
    private String destinationUri;

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * subscription priority by using this URI param. Lower numbers have higher priority.
     */
    @UriParam(label = "control", description = "The subscription priority.")
    private Integer priority;

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * {@link Predicate} by using this URI param. Only predicates that can be expressed as a string (e.g., using the
     * Simple language) can be specified via URI param. Other types must be sent via control channel POJO or as the
     * message body.
     */
    @UriParam(label = "control", description = "The subscription predicate.")
    private String predicate;

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * {@link Predicate} by using this URI param. Specify the predicate with bean syntax; i.e., #bean:someBeanId
     */
    @UriParam(label = "control", description = "A Predicate instance in the registry.", javaType = "org.apache.camel.Predicate")
    private Predicate predicateBean;

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * expression language for creating the {@link Predicate} by using this URI param. The default is "simple".
     */
    @UriParam(label = "control", defaultValue = "simple", description = "The subscription predicate language.")
    private String expressionLanguage = "simple";

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * action (subscribe or unsubscribe) by using this URI path variable.
     *
     * @return the control action -- subscribe or unsubscribe
     */
    public String getControlAction() {
        return controlAction;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * action (subscribe or unsubscribe) by using this URI path variable.
     *
     * @param controlAction the control action -- subscribe or unsubscribe
     */
    public void setControlAction(final String controlAction) {
        this.controlAction = controlAction;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * subscribe channel by using this URI path variable.
     *
     * @return subscribe channel name
     */
    public String getSubscribeChannel() {
        return subscribeChannel;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * subscribe channel by using this URI path variable.
     *
     * @param subscribeChannel subscribe channel name
     */
    public void setSubscribeChannel(final String subscribeChannel) {
        this.subscribeChannel = subscribeChannel;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * subscription ID by using this URI param. If one is not supplied, one will be generated and returned.
     *
     * @return the subscription ID
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * subscription ID by using this URI param. If one is not supplied, one will be generated and returned.
     *
     * @param subscriptionId the subscription ID
     */
    public void setSubscriptionId(final String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * destination URI by using this URI param.
     *
     * @return the destination URI
     */
    public String getDestinationUri() {
        return destinationUri;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * destination URI by using this URI param.
     *
     * @param destinationUri the destination URI
     */
    public void setDestinationUri(final String destinationUri) {
        this.destinationUri = destinationUri;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * subscription priority by using this URI param. Lower numbers have higher priority.
     *
     * @return the subscription priority
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * subscription priority by using this URI param. Lower numbers have higher priority.
     *
     * @param priority the subscription priority
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * {@link Predicate} by using this URI param. Only predicates that can be expressed as a string (e.g., using the
     * Simple language) can be specified via URI param. Other types must be sent via control channel POJO or as the
     * message body.
     *
     * @return the predicate for evaluating exchanges
     */
    public String getPredicate() {
        return predicate;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * {@link Predicate} by using this URI param. Only predicates that can be expressed as a string (e.g., using the
     * Simple language) can be specified via URI param. Other types must be sent via control channel POJO or as the
     * message body.
     *
     * @param predicate the predicate for evaluating exchanges
     */
    public void setPredicate(final String predicate) {
        this.predicate = predicate;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * {@link Predicate} by using this URI param. Only predicates that can be expressed as a string (e.g., using the
     * Simple language) can be specified via URI param. Other types must be sent via control channel POJO or as the
     * message body.
     *
     * @return the predicate for evaluating exchanges
     */
    public Predicate getPredicateBean() {
        return predicateBean;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * {@link Predicate} by using this URI param. Only predicates that can be expressed as a string (e.g., using the
     * Simple language) can be specified via URI param. Other types must be sent via control channel POJO or as the
     * message body.
     *
     * @param predicateBean the predicate for evaluating exchanges
     */
    public void setPredicateBean(final Predicate predicateBean) {
        this.predicateBean = predicateBean;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * expression language for creating the {@link Predicate} by using this URI param. The default is "simple".
     *
     * @return the expression language name
     */
    public String getExpressionLanguage() {
        return expressionLanguage;
    }

    /**
     * When sending messages to the control channel without using a {@link DynamicRouterControlMessage}, specify the
     * expression language for creating the {@link Predicate} by using this URI param. The default is "simple".
     *
     * @param expressionLanguage the expression language name
     */
    public void setExpressionLanguage(final String expressionLanguage) {
        this.expressionLanguage = expressionLanguage;
    }

    public DynamicRouterControlConfiguration() {

    }

    public DynamicRouterControlConfiguration copy() {
        try {
            return (DynamicRouterControlConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
