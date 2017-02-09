/*
  $Id: $
  @file RequireAuthError.java
  @brief Contains the RequireAuthError.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.registry;

import java.net.URI;
import java.util.Map;
import java.util.HashMap;

import com.distelli.webserver.RequestContext;

import lombok.extern.log4j.Log4j;

@Log4j
public class RequireAuthError extends RegistryError
{
    private static final String SERVICE_NAME = "distelli.docker-registry";

    private URI auth;
    public RequireAuthError(String message, URI auth) {
        super(message, RegistryErrorCode.UNAUTHORIZED);
        this.auth = auth;
    }
    public Map<String, String> getResponseHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Docker-Distribution-Api-Version", "registry/2.0");
        headers.put("WWW-Authenticate",
                    String.format("Bearer realm=\"%s\",service=\"%s\"",
                                  auth.toString(),
                                  SERVICE_NAME));
        return headers;
    }

    public static void throwRequireAuth(String message, RequestContext context) throws RegistryError {
        String host = context.getHost(null);
        int port = context.getPort();
        URI self;
        try {
            self = new URI(context.getProto(), null, host, port, "/v2/token", null, null);
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
        throw new RequireAuthError(message, self);
    }
}
