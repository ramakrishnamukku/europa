package com.distelli.europa.ajax;

import javax.inject.Inject;

import com.distelli.europa.Constants;
import com.distelli.europa.db.TokenAuthDb;
import com.distelli.europa.models.*;
import com.distelli.europa.util.*;
import com.distelli.persistence.PageIterator;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.JsonSuccess;
import com.distelli.webserver.RequestContext;
import com.google.inject.Singleton;
import com.distelli.europa.EuropaRequestContext;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class SetAuthTokenStatus extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private TokenAuthDb _tokenAuthDb;

    public SetAuthTokenStatus()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        TokenAuth tokenAuth = ajaxRequest.convertContent(TokenAuth.class, true);
        FieldValidator.validateNonNull(tokenAuth, "token", "status");

        _tokenAuthDb.setStatus(requestContext.getOwnerDomain(),
                               tokenAuth.getToken(),
                               tokenAuth.getStatus());
        return JsonSuccess.Success;
    }
}
