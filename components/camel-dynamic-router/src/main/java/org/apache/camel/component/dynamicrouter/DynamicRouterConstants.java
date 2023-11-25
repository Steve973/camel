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

import org.apache.camel.Expression;
import org.apache.camel.support.builder.ExpressionBuilder;

/**
 * Contains constants that are used within this component.
 */
public abstract class DynamicRouterConstants {

    /**
     * The camel version where the dynamic router eip component was first introduced.
     */
    public static final String FIRST_VERSION = "3.15.0";

    /**
     * The component name/scheme for the {@link DynamicRouterEndpoint}.
     */
    public static final String COMPONENT_SCHEME_ROUTING = "dynamic-router";

    /**
     * The title, for the auto-generated documentation.
     */
    public static final String TITLE = "Dynamic Router";

    /**
     * The mode for sending an exchange to recipients: send only to the first match.
     */
    public static final String MODE_FIRST_MATCH = "firstMatch";

    /**
     * The mode for sending an exchange to recipients: send to all matching.
     */
    public static final String MODE_ALL_MATCH = "allMatch";

    /**
     * The syntax, for the auto-generated documentation.
     */
    public static final String SYNTAX = COMPONENT_SCHEME_ROUTING + ":channel";

    public static final String RECIPIENT_LIST_HEADER = "DynamicRouterRecipientList";

    public static final Expression RECIPIENT_LIST_EXPRESSION = ExpressionBuilder.headerExpression(RECIPIENT_LIST_HEADER);
}
