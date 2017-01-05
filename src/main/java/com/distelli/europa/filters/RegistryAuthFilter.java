/*
  $Id: $
  @file RegistryAuthFilter.java
  @brief Contains the RegistryAuthFilter.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.filters;

import com.distelli.webserver.*;
import lombok.extern.log4j.Log4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.eclipse.jetty.http.HttpMethod;
import com.distelli.webserver.RequestHandler;
import com.distelli.webserver.WebResponse;
import com.distelli.webserver.RequestContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.distelli.europa.registry.RegistryAuth;
import com.distelli.europa.registry.RegistryError;
import javax.inject.Singleton;
import java.util.Map;
import java.util.HashMap;
import javax.inject.Inject;

@Log4j
public class RegistryAuthFilter implements RequestFilter<RequestContext>
{
    @Inject
    private RegistryAuth _auth;

    private static ObjectMapper OM = new ObjectMapper();
    static {
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    public RegistryAuthFilter()
    {

    }

    public WebResponse filter(RequestContext requestContext, RequestFilterChain next)
    {
        try {
            _auth.authenticate(requestContext);
            return next.filter(requestContext);
        } catch ( RegistryError ex ) {
            return handleError(ex);
        } catch ( Throwable ex ) {
            log.error(ex.getMessage(), ex);
            return handleError(new RegistryError(ex));
        }
    }

    private WebResponse handleError(RegistryError error) {
        WebResponse response = WebResponse.toJson(error.getResponseBody(), error.getErrorCode().getStatusCode());
        for ( Map.Entry<String, String> entry : error.getResponseHeaders().entrySet() ) {
            response.setResponseHeader(entry.getKey(), entry.getValue());
        }
        return response;
    }
}
