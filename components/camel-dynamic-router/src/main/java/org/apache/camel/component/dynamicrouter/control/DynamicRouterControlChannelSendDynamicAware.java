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

import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.spi.annotations.SendDynamic;
import org.apache.camel.support.component.SendDynamicAwareSupport;
import org.apache.camel.util.URISupport;

import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.COMPONENT_SCHEME_CONTROL;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_ACTION;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_DESTINATION_URI;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_EXPRESSION_LANGUAGE;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_PREDICATE;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_PREDICATE_BEAN;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_PRIORITY;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_SUBSCRIBE_CHANNEL;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_SUBSCRIPTION_ID;

@SendDynamic("dynamic-router-control")
public class DynamicRouterControlChannelSendDynamicAware extends SendDynamicAwareSupport {

    static final Pattern CONTROL_URI_BASE_PATTERN = Pattern.compile("^" + COMPONENT_SCHEME_CONTROL + ":");

    static final Map<String, String> URI_PARAMS_TO_HEADER_NAMES = Map.of(
            "controlAction", CONTROL_ACTION,
            "subscribeChannel", CONTROL_SUBSCRIBE_CHANNEL,
            "subscriptionId", CONTROL_SUBSCRIPTION_ID,
            "destinationUri", CONTROL_DESTINATION_URI,
            "priority", CONTROL_PRIORITY,
            "predicate", CONTROL_PREDICATE,
            "predicateBean", CONTROL_PREDICATE_BEAN,
            "expressionLanguage", CONTROL_EXPRESSION_LANGUAGE);

    static final Function<String, Matcher> OPTIMIZE_MATCHER = CONTROL_URI_BASE_PATTERN::matcher;

    static final Function<String, Boolean> SHOULD_OPTIMIZE = uri -> OPTIMIZE_MATCHER.apply(uri).find();

    @Override
    public boolean isLenientProperties() {
        return false;
    }

    @Override
    public DynamicAwareEntry prepare(Exchange exchange, String uri, String originalUri) throws Exception {
        Map<String, Object> properties = endpointProperties(exchange, uri);
        URI normalizedUri = URISupport.normalizeUriAsURI(uri);
        String controlAction = URISupport.extractRemainderPath(normalizedUri, false);
        if (controlAction.contains("://")) {
            controlAction = controlAction.substring(controlAction.indexOf("://") + 3);
        }
        properties.put("controlAction", controlAction);
        String propString = properties.entrySet().stream()
                .map(e -> String.format("%s->%s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(", ", "[", "]"));
        System.err.println("########## Preparing SendDynamicAware: properties = " + propString);
        return new DynamicAwareEntry(uri, originalUri, properties, null);
    }

    @Override
    public String resolveStaticUri(Exchange exchange, DynamicAwareEntry entry) {
        String optimizedUri = null;
        String uri = entry.getUri();
        if (SHOULD_OPTIMIZE.apply(uri)) {
            optimizedUri = URISupport.stripQuery(uri);
        }
        return optimizedUri;
    }

    @Override
    public Processor createPreProcessor(Exchange exchange, DynamicAwareEntry entry) {
        Processor preProcessor = null;
        if (SHOULD_OPTIMIZE.apply(entry.getUri())) {
            System.err.println("########## Creating SendDynamicAware pre-processor");
            preProcessor = ex -> {
                Map<String, Object> entryProperties = entry.getProperties();
                Message message = ex.getMessage();
                URI_PARAMS_TO_HEADER_NAMES.forEach((paramName, headerName) -> {
                    if (entryProperties.containsKey(paramName)) {
                        message.setHeader(headerName, entryProperties.get(paramName));
                    }
                });
            };

        }
        return preProcessor;
    }

    @Override
    public Processor createPostProcessor(Exchange exchange, DynamicAwareEntry entry) {
        Processor postProcessor = null;
        if (SHOULD_OPTIMIZE.apply(entry.getUri())) {
            System.err.println("########## Creating SendDynamicAware post-processor");
            postProcessor = ex -> {
                Message message = exchange.getMessage();
                URI_PARAMS_TO_HEADER_NAMES.values().forEach(message::removeHeader);
            };
        }
        return postProcessor;
    }
}
