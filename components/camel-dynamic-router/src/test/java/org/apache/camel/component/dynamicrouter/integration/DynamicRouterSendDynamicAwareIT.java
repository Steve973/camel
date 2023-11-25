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
package org.apache.camel.component.dynamicrouter.integration;

import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class DynamicRouterSendDynamicAwareIT extends CamelTestSupport {

    @Test
    public void testSendDynamic() throws Exception {
        MockEndpoint mock1 = getMockEndpoint("mock:result");
        mock1.expectedMinimumMessageCount(1);

        template.sendBodyAndHeaders("direct:start", "",
                Map.of("subscriptionId", "testSubscription1",
                        "destinationUri", mock1.getEndpointUri(),
                        "priority", 1,
                        "predicate", "${body} contains 'test'"));

        MockEndpoint.assertIsSatisfied(context);
    }

    //    /**
    //     * Tests participant subscription, and that messages are received at their registered destination endpoints.
    //     *
    //     * @throws Exception if interrupted while waiting for mocks to be satisfied
    //     */
    //    @Test
    //    public void testSubscribeWithUriAndMultipleSubscribers() throws Exception {
    //        MockEndpoint mock1 = getMockEndpoint("mock:result1");
    //        MockEndpoint mock2 = getMockEndpoint("mock:result2");
    //        mock1.expectedMinimumMessageCount(1);
    //        mock2.expectedMinimumMessageCount(1);
    //
    //        String subscribeUri1 = createControlChannelUri(CONTROL_ACTION_SUBSCRIBE, "testSubscription1",
    //                mock1.getEndpointUri(), 1, "${body} contains 'Message1'");
    //        template.sendBody(subscribeUri1, "");
    //
    //        String subscribeUri2 = createControlChannelUri(CONTROL_ACTION_SUBSCRIBE, "testSubscription2",
    //                mock2.getEndpointUri(), 1, "${body} contains 'Message2'");
    //        template.sendBody(subscribeUri2, "");
    //
    //        // Trigger events to subscribers
    //        template.sendBody("direct:start", "testMessage1");
    //        template.sendBody("direct:start", "testMessage2");
    //
    //        MockEndpoint.assertIsSatisfied(context, 5, TimeUnit.SECONDS);
    //    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct://start")
                        .toD("dynamic-router://control/subscribe/test?" +
                             "subscriptionId=${header.subscriptionId}" +
                             "&destinationUri=${header.destinationUri}" +
                             "&priority=${header.priority}" +
                             "&predicate=${header.predicate}");
            }
        };
    }
}
