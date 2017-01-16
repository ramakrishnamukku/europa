package com.distelli.europa.ajax;

import javax.inject.Inject;
import javax.persistence.RollbackException;

import com.distelli.europa.Constants;
import com.distelli.europa.db.TokenAuthDb;
import com.distelli.europa.models.*;
import com.distelli.europa.util.*;
import com.distelli.persistence.PageIterator;
import com.distelli.webserver.AjaxClientException;
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
public class DeleteAuthToken extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private TokenAuthDb _tokenAuthDb;

    public DeleteAuthToken()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String token = ajaxRequest.getParam("token", true); //throw if missing
        try {
            _tokenAuthDb.deleteToken(requestContext.getOwnerDomain(), token);
        } catch(RollbackException rbe) {
            throw(new AjaxClientException("Cannot Delete active Token", AjaxErrors.Codes.TokenIsActive, 400));
        }
        return JsonSuccess.Success;
    }
}
