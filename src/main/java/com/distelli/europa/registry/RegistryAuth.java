package com.distelli.europa.registry;

import java.io.UnsupportedEncodingException;
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
        if ( "basic".equals(scheme)) {
            basicAuth(context, tokenizer.nextToken());
            return;
        } else if("bearer".equals(scheme)) {
            bearerAuth(context, tokenizer.nextToken());
            return;
        }
        if(log.isDebugEnabled())
            log.debug("Unsupported Authorization scheme="+scheme);
        RequireAuthError.throwRequireAuth("Unsupported Authorization scheme="+scheme, context);
    }

    private void bearerAuth(EuropaRequestContext context, String tokenStr) {
        String decodedToken = getDecodedToken(tokenStr, context);
        if(RegistryToken.isPublicToken(decodedToken))
        {
            context.setRegistryApiToken(decodedToken);
            return;
        }

        //validate api token
        if(isValidApiToken(decodedToken, context))
            return;
        RequireAuthError.throwRequireAuth("Invalid username or password", context);
    }

    private void basicAuth(EuropaRequestContext context, String tokenStr) {
        String decodedToken = getDecodedToken(tokenStr, context);

        String[] tokenParts = decodedToken.split(":", 2);
        if(tokenParts.length != 2)
        {
            if(log.isDebugEnabled())
                log.debug("Illegal Basic Auth Token: "+decodedToken);
            RequireAuthError.throwRequireAuth("Illegal basic auth token", context);
        }
        String user = tokenParts[0];
        String passwd = tokenParts[1];
        if ( "TOKEN".equals(user) ) {
            TokenAuth tokenAuth = _tokenAuthDb.getToken(passwd);
            if(tokenAuth != null && tokenAuth.getStatus() == TokenAuthStatus.ACTIVE) {
                context.setRemoteUser(tokenAuth.getDomain());
                context.setRequesterDomain(tokenAuth.getDomain());
                context.setRegistryApiToken(tokenAuth.getToken());
                return;
            }
        }
        RequireAuthError.throwRequireAuth("Invalid username or password", context);
    }

    private boolean isValidApiToken(String token, EuropaRequestContext context)
    {
        TokenAuth tokenAuth = _tokenAuthDb.getToken(token);
        if(tokenAuth == null || tokenAuth.getStatus() != TokenAuthStatus.ACTIVE)
            return false;
        context.setRemoteUser(tokenAuth.getDomain());
        context.setRequesterDomain(tokenAuth.getDomain());
        context.setRegistryApiToken(tokenAuth.getToken());
        return true;
    }

    private String getDecodedToken(String tokenStr, EuropaRequestContext context)
    {
        byte[] tokenBytes;
        try {
            tokenBytes = Base64.getDecoder().decode(tokenStr);
        } catch(IllegalArgumentException ex) {
            log.debug("Illegal Basic token="+tokenStr, ex);
            tokenBytes = null;
        }

        if(tokenBytes == null)
            RequireAuthError.throwRequireAuth("Illegal Basic token="+tokenStr, context);

        String decodedToken = null;
        try {
            decodedToken = new String(tokenBytes, "UTF-8");
        } catch(UnsupportedEncodingException usee) {
            throw(new RuntimeException(usee));
        }
        return decodedToken;
    }
}
