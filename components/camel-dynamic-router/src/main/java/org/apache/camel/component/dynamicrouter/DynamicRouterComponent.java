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

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.camel.Endpoint;
import org.apache.camel.Predicate;
import org.apache.camel.component.dynamicrouter.DynamicRouterProcessor.DynamicRouterProcessorFactory;
import org.apache.camel.component.dynamicrouter.DynamicRouterProducer.DynamicRouterProducerFactory;
import org.apache.camel.component.dynamicrouter.PrioritizedFilter.PrioritizedFilterFactory;
import org.apache.camel.component.dynamicrouter.control.DynamicRouterControlMessage;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;
import org.apache.camel.support.service.ServiceHelper;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.component.dynamicrouter.DynamicRouterEndpoint.DynamicRouterEndpointFactory;

/**
 * The Dynamic Router {@link org.apache.camel.Component}. Manages:
 * <ul>
 * <li>{@link org.apache.camel.Consumer}s: addition and removal</li>
 * <li>Control Channel: manage routing participants and their routing rules</li>
 * <li>Endpoint: creates the {@link Endpoint} for a Dynamic Router URI</li>
 * </ul>
 */
@Component("dynamic-router")
public class DynamicRouterComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicRouterComponent.class);

    /**
     * The {@link DynamicRouterProcessor}s, mapped by their channel, for the Dynamic Router.
     */
    private final Map<String, DynamicRouterProcessor> processors = new HashMap<>();

    /**
     * Lists of {@link PrioritizedFilter}s, mapped by their channel.
     * <p>
     * Each list holds the filters for that routing channel.
     */
    private final Map<String, TreeSet<PrioritizedFilter>> filterMap = new HashMap<>();

    /**
     * Creates a {@link DynamicRouterEndpoint} instance.
     */
    private Supplier<DynamicRouterEndpointFactory> endpointFactorySupplier = DynamicRouterEndpointFactory::new;

    /**
     * Creates a {@link DynamicRouterProcessor} instance.
     */
    private Supplier<DynamicRouterProcessorFactory> processorFactorySupplier = DynamicRouterProcessorFactory::new;

    /**
     * Creates a {@link DynamicRouterProducer} instance.
     */
    private Supplier<DynamicRouterProducerFactory> producerFactorySupplier = DynamicRouterProducerFactory::new;

    /**
     * Creates a {@link PrioritizedFilter} instance.
     */
    private Supplier<PrioritizedFilterFactory> filterProcessorFactorySupplier = PrioritizedFilterFactory::new;

    /**
     * Create an instance of the Dynamic Router component.
     */
    public DynamicRouterComponent() {
        LOG.debug("Created Dynamic Router component");
    }

    /**
     * Create an instance of the Dynamic Router component with custom factories.
     *
     * @param endpointFactorySupplier        creates the {@link DynamicRouterEndpoint}
     * @param processorFactorySupplier       creates the {@link DynamicRouterProcessor}
     * @param producerFactorySupplier        creates the {@link DynamicRouterProducer}
     * @param filterProcessorFactorySupplier creates the {@link PrioritizedFilter}
     */
    public DynamicRouterComponent(
            final Supplier<DynamicRouterEndpointFactory> endpointFactorySupplier,
            final Supplier<DynamicRouterProcessorFactory> processorFactorySupplier,
            final Supplier<DynamicRouterProducerFactory> producerFactorySupplier,
            final Supplier<PrioritizedFilterFactory> filterProcessorFactorySupplier) {
        this.endpointFactorySupplier = endpointFactorySupplier;
        this.processorFactorySupplier = processorFactorySupplier;
        this.producerFactorySupplier = producerFactorySupplier;
        this.filterProcessorFactorySupplier = filterProcessorFactorySupplier;
        LOG.debug("Created Dynamic Router component");
    }

    /**
     * Create an endpoint for the supplied URI, and with the supplied parameters. The control channel URI
     *
     * @param uri        endpoint URI
     * @param remaining  portion of the URI after the scheme, and before parameters (the channel)
     * @param parameters URI parameters
     * @return an endpoint for the supplied URI
     */
    @Override
    protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters)
            throws Exception {
        DynamicRouterConfiguration configuration = new DynamicRouterConfiguration();
        DynamicRouterEndpoint endpoint;
        if (remaining == null || remaining.isBlank()) {
            throw new IllegalArgumentException("You must provide a channel for the Dynamic Router");
        }
        endpoint = endpointFactorySupplier.get().getInstance(uri, this, configuration,
                processorFactorySupplier, producerFactorySupplier);
        setProperties(endpoint, parameters);
        return endpoint;
    }

    /**
     * Perform shutdown on the Dynamic Router.
     *
     * @throws Exception to indicate a problem with shutdown
     */
    @Override
    protected void doShutdown() throws Exception {
        ServiceHelper.stopAndShutdownService(processors);
        processors.clear();
        super.doShutdown();
    }

    /**
     * If no processor has been added for the supplied channel, then add the supplied processor.
     *
     * @param channel   the channel to add the processor to
     * @param processor the processor to add for the channel
     */
    void addRoutingProcessor(final String channel, final DynamicRouterProcessor processor) {
        if (processors.putIfAbsent(channel, processor) != null) {
            throw new IllegalArgumentException(
                    "Dynamic Router can have only one processor per channel; channel '"
                            + channel + "' already has a processor");
        }
    }

    TreeSet<PrioritizedFilter> getChannelFilters(final String channel) {
        return filterMap.get(channel);
    }

    /**
     * Get the processor for the given channel.
     *
     * @param channel the channel to get the processor for
     * @return the processor for the given channel
     */
    public DynamicRouterProcessor getRoutingProcessor(final String channel) {
        return processors.get(channel);
    }

    /**
     * Convenience method to create a {@link PrioritizedFilter} from the details of the incoming
     * {@link DynamicRouterControlMessage} properties.
     *
     * @param controlMessage the incoming control message
     * @return a {@link PrioritizedFilter} built from the properties of the incoming control message
     */
    PrioritizedFilter createFilter(final DynamicRouterControlMessage controlMessage) {
        final String id = controlMessage.id();
        final int priority = controlMessage.priority();
        final String endpoint = controlMessage.endpoint();
        final Predicate predicate = controlMessage.predicate();
        return filterProcessorFactorySupplier.get().getInstance(id, priority, predicate, endpoint);
    }

    /**
     * Add a filter based on the supplied control message properties for exchange routing evaluation.
     *
     * @param controlMessage the message for filter creation
     */
    public void addFilter(final DynamicRouterControlMessage controlMessage) {
        addFilter(createFilter(controlMessage), controlMessage.channel());
    }

    /**
     * Adds the filter to the list of filters, and ensure that the filters are sorted by priority after the insertion.
     *
     * @param filter the filter to add
     */
    public void addFilter(final PrioritizedFilter filter, final String channel) {
        synchronized (filterMap) {
            if (filter != null) {
                Set<PrioritizedFilter> filters = filterMap.computeIfAbsent(channel, c -> new TreeSet<>(PrioritizedFilter.COMPARATOR));
                filters.add(filter);
                LOG.debug("Added subscription: {}", filter);
            }
        }
    }

    /**
     * Return the filter with the supplied filter identifier. If there is no such filter, then return null.
     *
     * @param filterId the filter identifier
     * @return the filter with the supplied ID, or null
     */
    public PrioritizedFilter getFilter(final String filterId, final String channel) {
        Set<PrioritizedFilter> filters = Optional.ofNullable(channel)
                .filter(ObjectHelper::isNotEmpty)
                .map(filterMap::get)
                .orElse(filterMap.values().stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toCollection(() -> new TreeSet<>(PrioritizedFilter.COMPARATOR))));
        return filters.stream()
                .filter(f -> filterId.equals(f.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No filter exists with ID: " + filterId));
    }

    /**
     * Removes a filter with the ID from the control message.
     *
     * @param filterId the ID of the filter to remove
     */
    public void removeFilter(final String filterId) {
        synchronized (filterMap) {
            Optional.ofNullable(filterMap.remove(filterId))
                    .ifPresentOrElse(
                            f -> LOG.debug("Removed subscription: {}", f),
                            () -> LOG.debug("No subscription exists with ID: {}", filterId));
        }
    }
}
