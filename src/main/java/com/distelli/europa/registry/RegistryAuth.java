package com.distelli.europa.registry;

import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.inject.Inject;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.TokenAuthDb;
import com.distelli.europa.models.TokenAuth;
import com.distelli.europa.models.TokenAuthStatus;
import com.distelli.webserver.RequestContext;

import lombok.extern.log4j.Log4j;
import static java.nio.charset.StandardCharsets.UTF_8;

@Log4j
public class RegistryAuth {
    private static final String SERVICE_NAME = "distelli.docker-registry";

    @Inject
    private TokenAuthDb _tokenAuthDb;

    public void authenticate(EuropaRequestContext context) throws RegistryError {
        String authorization = context.getHeaderValue("Authorization");
        if ( null == authorization ) {
            log.debug("Missing authorization header");
            requireAuth("Missing Authorization header", context);
        }
        StringTokenizer tokenizer = new StringTokenizer(authorization);
        String scheme = tokenizer.hasMoreTokens() ? tokenizer.nextToken().toLowerCase() : null;
        if ( "basic".equals(scheme) ) {
            basicAuth(context, tokenizer.nextToken());
            return;
        }
        log.debug("Unsupported Authorization scheme="+scheme);
        requireAuth("Unsupported Authorization scheme="+scheme, context);
    }

    private void basicAuth(EuropaRequestContext context, String tokenStr) {
        byte[] token;
        try {
            token = Base64.getDecoder().decode(tokenStr);
        } catch ( IllegalArgumentException ex ) {
            log.debug("Illegal Basic token="+tokenStr, ex);
            requireAuth("Illegal Basic token="+tokenStr, context);
            return;
        }
        int colon = 0;
        for (;;colon++) {
            if ( colon >= token.length ) {
                colon = -1;
                break;
            }
            if ( ':' == token[colon] ) {
                break;
            }
        }
        if ( colon < 0 ) {
            log.debug("Illegal basic token missing :");
            requireAuth("Illegal basic token missing ':'", context);
        }
        String user = new String(token, 0, colon, UTF_8);
        String passwd = new String(token, colon+1, token.length-colon-1, UTF_8);
        if ( "TOKEN".equals(user) ) {
            TokenAuth tokenAuth = _tokenAuthDb.getToken(passwd);
            if(tokenAuth != null && tokenAuth.getStatus() == TokenAuthStatus.ACTIVE) {
                context.setRemoteUser(tokenAuth.getDomain());
                context.setRequesterDomain(tokenAuth.getDomain());
                return;
            }
        }
        requireAuth("Invalid username or password", context);
    }

    private static class RequireAuthError extends RegistryError {
        private URI auth;
        public RequireAuthError(String message, URI auth) {
            super(message, RegistryErrorCode.UNAUTHORIZED);
            this.auth = auth;
        }
        public Map<String, String> getResponseHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put("Docker-Distribution-Api-Version", "registry/2.0");
            headers.put(
                "WWW-Authenticate",
                String.format(
                    "Basic realm=\"%s\",service=\"%s\"",
                    auth.toString(),
                    SERVICE_NAME));
            return headers;
        }
    }

    private void requireAuth(String message, RequestContext context) throws RegistryError {
        String host = context.getHost(null);
        int port = context.getPort();
        URI self;
        try {
            self = new URI(context.getProto(), null, host, port, "/", null, null);
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
        throw new RequireAuthError(message, self);
    }
}
