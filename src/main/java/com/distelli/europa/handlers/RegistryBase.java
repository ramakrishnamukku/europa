package com.distelli.europa.handlers;

import com.distelli.europa.db.UserDb;
import com.distelli.europa.models.User;
import com.distelli.europa.registry.RegistryAuth;
import com.distelli.europa.registry.RegistryError;
import com.distelli.webserver.RequestContext;
import com.distelli.webserver.RequestHandler;
import com.distelli.webserver.WebResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.log4j.Log4j;
import org.eclipse.jetty.http.HttpMethod;

@Log4j
@Singleton
public abstract class RegistryBase extends RequestHandler
{
    protected static int DEFAULT_PAGE_SIZE = 100;

    abstract public WebResponse handleRegistryRequest(RequestContext requestContext);

    private static final ObjectMapper OM = new ObjectMapper();

    @Inject
    private UserDb _userDb;

    public WebResponse handleRequest(RequestContext requestContext) {
        try {
            return handleRegistryRequest(requestContext);
        } catch ( RegistryError ex ) {
            return handleError(ex);
        } catch ( Throwable ex ) {
            log.error(ex.getMessage(), ex);
            return handleError(new RegistryError(ex));
        }
    }

    private WebResponse handleError(RegistryError error) {
        if ( log.isInfoEnabled() ) {
            try {
                log.info("RegistryError: "+error.getErrorCode()+" "+OM.writeValueAsString(error.getResponseBody()));
            } catch ( Exception ex ){
                log.error(ex.getMessage(), ex);
            }
        }
        WebResponse response = toJson(error.getResponseBody(), error.getStatusCode());
        for ( Map.Entry<String, String> entry : error.getResponseHeaders().entrySet() ) {
            response.setResponseHeader(entry.getKey(), entry.getValue());
        }
        return response;
    }

    protected static void pump(InputStream in, OutputStream out) throws IOException {
        byte[] buff = new byte[1024*1024];
        while ( true ) {
            int len=in.read(buff);
            if ( len <= 0 ) break;
            out.write(buff, 0, len);
        }
    }

    protected String getDomainForOwner(String owner) {
        if ( null == owner ) return null;
        User user = _userDb.getUserByUsername(owner);
        if ( null == user ) return null;
        return user.getDomain();
    }

    protected String joinWithSlash(String... parts) {
        if ( null == parts || parts.length <= 0 ) return "";
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for ( String part : parts ) {
            if ( null == part ) continue;
            if ( isFirst ) {
                isFirst = false;
            } else {
                sb.append("/");
            }
            sb.append(part);
        }
        return sb.toString();
    }

    protected int getPageSize(RequestContext requestContext) {
        try {
            return Integer.parseInt(requestContext.getParameter("n"));
        } catch ( NumberFormatException ex ) {}
        return DEFAULT_PAGE_SIZE;
    }
}
