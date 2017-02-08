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
    @Inject
    private TokenAuthDb _tokenAuthDb;

    public void authenticate(EuropaRequestContext context) throws RegistryError {
        String authorization = context.getHeaderValue("Authorization");
        if ( null == authorization ) {
            //If the authorization header is missing that means its an
            //anonymous request. Don't do the auth check and let the
            //request pass. The RegistryAccess class will check if the
            //request should be allowed
            context.setRemoteUser(null);
            context.setRequesterDomain(null);
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(authorization);
        String scheme = tokenizer.hasMoreTokens() ? tokenizer.nextToken().toLowerCase() : null;
        if ( "basic".equals(scheme) ) {
            basicAuth(context, tokenizer.nextToken());
            return;
        }
        if(log.isDebugEnabled())
            log.debug("Unsupported Authorization scheme="+scheme);
        RequireAuthError.throwRequireAuth("Unsupported Authorization scheme="+scheme, context);
    }

    private void basicAuth(EuropaRequestContext context, String tokenStr) {
        byte[] token;
        try {
            token = Base64.getDecoder().decode(tokenStr);
        } catch ( IllegalArgumentException ex ) {
            log.debug("Illegal Basic token="+tokenStr, ex);
            RequireAuthError.throwRequireAuth("Illegal Basic token="+tokenStr, context);
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
            RequireAuthError.throwRequireAuth("Illegal basic token missing ':'", context);
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
        RequireAuthError.throwRequireAuth("Invalid username or password", context);
    }
}
