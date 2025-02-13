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
package org.apache.camel.management;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.spi.EventNotifier;
import org.apache.camel.support.EventNotifierSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisabledOnOs(OS.AIX)
public class AddEventNotifierTest extends ContextTestSupport {

    private static List<CamelEvent> events = new ArrayList<>();
    private EventNotifier notifier;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        events.clear();
        super.setUp();
    }

    @Test
    public void testAddAndRemove() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(1);
        template.sendBody("direct:start", "Hello World");
        assertMockEndpointsSatisfied();

        assertEquals(0, events.size());

        // we should be able to add after CamelContext has been started
        notifier = new EventNotifierSupport() {
            public void notify(CamelEvent event) throws Exception {
                events.add(event);
            }
        };
        // must add notifier as a service so its started
        context.addService(notifier);
        context.getManagementStrategy().addEventNotifier(notifier);

        resetMocks();
        getMockEndpoint("mock:result").expectedMessageCount(1);
        template.sendBody("direct:start", "Bye World");
        assertMockEndpointsSatisfied();

        assertEquals(11, events.size());

        // remove and we should not get new events
        context.getManagementStrategy().removeEventNotifier(notifier);

        resetMocks();
        getMockEndpoint("mock:result").expectedMessageCount(1);
        template.sendBody("direct:start", "Hi World");
        assertMockEndpointsSatisfied();

        assertEquals(11, events.size());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("log:foo").to("mock:result");
            }
        };
    }

}
