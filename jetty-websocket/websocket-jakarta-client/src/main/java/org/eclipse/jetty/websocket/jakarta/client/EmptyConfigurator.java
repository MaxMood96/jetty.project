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

package org.eclipse.jetty.websocket.jakarta.client;

import java.util.List;
import java.util.Map;

import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.HandshakeResponse;

public class EmptyConfigurator extends ClientEndpointConfig.Configurator
{
    public static final EmptyConfigurator INSTANCE = new EmptyConfigurator();

    @Override
    public void afterResponse(HandshakeResponse hr)
    {
        // do nothing
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers)
    {
        // do nothing
    }
}
