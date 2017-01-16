package com.distelli.europa.handlers;

import com.distelli.europa.EuropaRequestContext;
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
public class RegistryLayerDelete extends RegistryBase {
    public WebResponse handleRegistryRequest(EuropaRequestContext requestContext) {
        WebResponse response = new WebResponse(200);
        return response;
    }
}
