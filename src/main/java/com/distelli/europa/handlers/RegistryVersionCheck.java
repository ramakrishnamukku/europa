package com.distelli.europa.handlers;

import org.eclipse.jetty.http.HttpMethod;
import com.distelli.webserver.RequestHandler;
import com.distelli.webserver.WebResponse;
import com.distelli.webserver.RequestContext;
import com.fasterxml.jackson.databind.JsonNode;
import javax.inject.Singleton;
import java.util.Map;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class RegistryVersionCheck extends RegistryBase {
    // This is used for testing RegistryBase (throw internal server error)
    private RegistryBase override = null;
    public void overrideImplementation(RegistryBase override) {
        this.override = override;
    }
    public WebResponse handleRegistryRequest(RequestContext requestContext) {
        if ( null != override ) return override.handleRegistryRequest(requestContext);

        WebResponse response = new WebResponse(200);
        response.setResponseHeader("Docker-Distribution-API-Version", "registry/2.0");
        return response;
    }
}
