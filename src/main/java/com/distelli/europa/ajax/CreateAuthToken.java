package com.distelli.europa.ajax;

import javax.inject.Inject;

import com.distelli.europa.Constants;
import com.distelli.europa.db.TokenAuthDb;
import com.distelli.europa.models.*;
import com.distelli.persistence.PageIterator;
import com.distelli.utils.CompactUUID;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.RequestContext;
import com.google.inject.Singleton;
import com.distelli.europa.EuropaRequestContext;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class CreateAuthToken extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private TokenAuthDb _tokenAuthDb;

    public CreateAuthToken()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        TokenAuth tokenAuth = TokenAuth
        .builder()
        .domain(requestContext.getRequesterDomain())
        .token(CompactUUID.randomUUID().toString())
        .status(TokenAuthStatus.ACTIVE)
        .created(System.currentTimeMillis())
        .build();

        _tokenAuthDb.save(tokenAuth);
        return tokenAuth.getToken();
    }
}
