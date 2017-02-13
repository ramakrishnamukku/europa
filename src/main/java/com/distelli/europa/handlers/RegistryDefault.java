package com.distelli.europa.handlers;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jetty.http.HttpMethod;
import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.registry.RegistryError;
import com.distelli.europa.registry.RegistryErrorCode;
import com.distelli.webserver.RequestContext;
import com.distelli.webserver.RequestHandler;
import com.distelli.webserver.WebResponse;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class RegistryDefault extends RegistryBase {
    public WebResponse handleRegistryRequest(EuropaRequestContext requestContext) {
        throw(new RegistryError("Invalid or unknown name",
                                RegistryErrorCode.NAME_UNKNOWN));
    }
}
