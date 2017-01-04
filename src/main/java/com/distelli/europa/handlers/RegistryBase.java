package com.distelli.europa.handlers;

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
import lombok.extern.log4j.Log4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

@Log4j
@Singleton
public abstract class RegistryBase extends RequestHandler
{
    private static ObjectMapper OM = new ObjectMapper();
    static {
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    @Inject
    private RegistryAuth _auth;

    abstract public WebResponse handleRegistryRequest(RequestContext requestContext);

    public WebResponse handleRequest(RequestContext requestContext) {
        try {
            _auth.authenticate(requestContext);
            return handleRegistryRequest(requestContext);
        } catch ( RegistryError ex ) {
            return handleError(ex);
        } catch ( Throwable ex ) {
            log.error(ex.getMessage(), ex);
            return handleError(new RegistryError(ex));
        }
    }

    private WebResponse handleError(RegistryError error) {
        WebResponse response = toJson(error.getResponseBody(), error.getErrorCode().getStatusCode());
        for ( Map.Entry<String, String> entry : error.getResponseHeaders().entrySet() ) {
            response.setResponseHeader(entry.getKey(), entry.getValue());
        }
        return response;
    }
}
