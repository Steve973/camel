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
import java.util.function.Supplier;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;

import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.COMPONENT_SCHEME_CONTROL;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_ENDPOINT_FACTORY_SUPPLIER;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlEndpoint.DynamicRouterControlEndpointFactory;

/**
 * The component for the Dynamic router control operations that allow routing participants to subscribe or unsubscribe
 * to participate in dynamic message routing.
 */
@Component(COMPONENT_SCHEME_CONTROL)
public class DynamicRouterControlComponent extends DefaultComponent {

    /**
     * The {@link Supplier<DynamicRouterControlEndpointFactory>} that gets an instance of the
     * {@link DynamicRouterControlEndpoint}, but is also settable to facilitate testing.
     */
    private Supplier<DynamicRouterControlEndpointFactory> controlEndpointFactorySupplier = CONTROL_ENDPOINT_FACTORY_SUPPLIER;

    /**
     * Holds the control endpoint/component configuration.
     */
    @Metadata(label = "control")
    protected DynamicRouterControlConfiguration configuration = new DynamicRouterControlConfiguration();

    /**
     * Default constructor to create the instance.
     */
    public DynamicRouterControlComponent() {

    }

    /**
     * Constructor to create the instance with the {@link CamelContext}.
     *
     * @param context the {@link CamelContext}
     */
    public DynamicRouterControlComponent(final CamelContext context) {
        super(context);
    }

    /**
     * Gets the {@link DynamicRouterControlConfiguration}.
     *
     * @return the {@link DynamicRouterControlConfiguration}
     */
    public DynamicRouterControlConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the {@link DynamicRouterControlConfiguration}
     *
     * @param configuration {@link DynamicRouterControlConfiguration}
     */
    public void setConfiguration(DynamicRouterControlConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Creates the {@link DynamicRouterControlEndpoint}.
     *
     * @param  uri        the URI that was used to trigger the endpoint creation
     * @param  remaining  the portion of the URI that comes after the component name, but before any query parameters
     * @param  parameters the URI query parameters
     * @return            the {@link DynamicRouterControlEndpoint}
     */
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        DynamicRouterControlConfiguration configuration = this.configuration.copy();
        DynamicRouterControlEndpoint endpoint
                = controlEndpointFactorySupplier.get().getInstance(uri, this, remaining, configuration);
        setProperties(endpoint, parameters);
        return endpoint;
    }

    /**
     * Sets the {@link Supplier<DynamicRouterControlEndpointFactory>} that gets an instance of the
     * {@link DynamicRouterControlEndpoint}, so that it is also settable to facilitate testing.
     *
     * @param controlEndpointFactorySupplier {@link Supplier<DynamicRouterControlEndpointFactory>}
     */
    public void setControlEndpointFactorySupplier(
            Supplier<DynamicRouterControlEndpointFactory> controlEndpointFactorySupplier) {
        this.controlEndpointFactorySupplier = controlEndpointFactorySupplier;
    }
}
