package com.distelli.europa.ajax;

import com.distelli.europa.util.*;
import com.distelli.europa.models.*;
import com.distelli.europa.db.TokenAuthDb;
import lombok.extern.log4j.Log4j;
import com.google.inject.Singleton;
import javax.inject.Inject;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.JsonSuccess;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.AjaxClientException;
import com.distelli.europa.Constants;

import com.distelli.persistence.PageIterator;
import javax.persistence.RollbackException;

@Log4j
@Singleton
public class DeleteAuthToken extends AjaxHelper
{
    @Inject
    private TokenAuthDb _tokenAuthDb;

    public DeleteAuthToken()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest)
    {
        String token = ajaxRequest.getParam("token",
                                            true); //throw if missing
        try {
            _tokenAuthDb.deleteToken(token);
        } catch(RollbackException rbe) {
            throw(new AjaxClientException("Cannot Delete active Token", AjaxErrors.Codes.TokenIsActive, 400));
        }
        return JsonSuccess.Success;
    }
}
