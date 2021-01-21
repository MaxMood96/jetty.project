//
// ========================================================================
// Copyright (c) 1995-2021 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package org.eclipse.jetty.websocket.jakarta.common;

import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import org.eclipse.jetty.websocket.core.CoreSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractSessionTest
{
    protected static JakartaWebSocketSession session;
    protected static JakartaWebSocketContainer container;

    @BeforeAll
    public static void initSession() throws Exception
    {
        container = new DummyContainer();
        container.start();
        Object websocketPojo = new DummyEndpoint();
        UpgradeRequest upgradeRequest = new UpgradeRequestAdapter();
        JakartaWebSocketFrameHandler frameHandler = container.newFrameHandler(websocketPojo, upgradeRequest);
        CoreSession coreSession = new CoreSession.Empty();
        session = new JakartaWebSocketSession(container, coreSession, frameHandler, container.getFrameHandlerFactory()
            .newDefaultEndpointConfig(websocketPojo.getClass()));
    }

    @AfterAll
    public static void stopContainer() throws Exception
    {
        container.stop();
    }

    public static class DummyEndpoint extends Endpoint
    {
        @Override
        public void onOpen(Session session, EndpointConfig config)
        {
        }
    }
}
