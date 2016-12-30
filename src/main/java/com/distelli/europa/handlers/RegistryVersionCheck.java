package com.distelli.europa.handlers;

import org.eclipse.jetty.http.HttpMethod;
import com.distelli.ventura.RequestHandler;
import com.distelli.ventura.WebResponse;
import com.distelli.ventura.RequestContext;
import com.fasterxml.jackson.databind.JsonNode;
import javax.inject.Singleton;
import java.util.Map;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class RegistryVersionCheck extends RegistryBase {
    public WebResponse handleRegistryRequest(RequestContext requestContext) {
        WebResponse response = new WebResponse(200);
        response.setResponseHeader("Docker-Distribution-API-Version", "registry/2.0");
        return response;
    }
}
