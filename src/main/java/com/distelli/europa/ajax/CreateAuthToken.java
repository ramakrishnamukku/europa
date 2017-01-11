package com.distelli.europa.ajax;

import com.distelli.europa.models.*;
import com.distelli.europa.db.TokenAuthDb;
import lombok.extern.log4j.Log4j;
import com.google.inject.Singleton;
import javax.inject.Inject;
import com.distelli.webserver.HTTPMethod;
import com.distelli.europa.Constants;
import com.distelli.persistence.PageIterator;
import com.distelli.utils.CompactUUID;

@Log4j
@Singleton
public class CreateAuthToken extends AjaxHelper
{
    @Inject
    private TokenAuthDb _tokenAuthDb;

    public CreateAuthToken()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest)
    {
        TokenAuth tokenAuth = TokenAuth
        .builder()
        .domain(Constants.DOMAIN_ZERO)
        .token(CompactUUID.randomUUID().toString())
        .status(TokenAuthStatus.ACTIVE)
        .created(System.currentTimeMillis())
        .build();

        _tokenAuthDb.save(tokenAuth);
        return tokenAuth.getToken();
    }
}
