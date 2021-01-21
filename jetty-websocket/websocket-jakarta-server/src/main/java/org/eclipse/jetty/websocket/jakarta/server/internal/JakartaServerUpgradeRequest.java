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

package org.eclipse.jetty.websocket.jakarta.server.internal;

import java.net.URI;
import java.security.Principal;

import org.eclipse.jetty.websocket.core.server.ServerUpgradeRequest;
import org.eclipse.jetty.websocket.jakarta.common.UpgradeRequest;

public class JakartaServerUpgradeRequest implements UpgradeRequest
{
    private final ServerUpgradeRequest servletRequest;

    public JakartaServerUpgradeRequest(ServerUpgradeRequest servletRequest)
    {
        this.servletRequest = servletRequest;
    }

    @Override
    public Principal getUserPrincipal()
    {
        return servletRequest.getUserPrincipal();
    }

    @Override
    public URI getRequestURI()
    {
        return servletRequest.getRequestURI();
    }

    @Override
    public String getPathInContext()
    {
        return servletRequest.getPathInContext();
    }
}
