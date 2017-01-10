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
public class RegistryManifestPush extends RegistryBase {
    public WebResponse handleRegistryRequest(RequestContext requestContext) {
        String name = requestContext.getMatchedRoute().getParam("name");
        String reference = requestContext.getMatchedRoute().getParam("reference");

        String location="TODO";
        String contentDigest="TODO";

        WebResponse response = new WebResponse();
        response.setHttpStatusCode(201);
        response.setResponseHeader("Location", location);
        response.setResponseHeader("Docker-Content-Digest", contentDigest);
        return response;
    }
}
