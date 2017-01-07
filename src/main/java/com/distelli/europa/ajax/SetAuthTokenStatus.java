package com.distelli.europa.ajax;

import com.distelli.europa.util.*;
import com.distelli.europa.models.*;
import com.distelli.europa.db.TokenAuthDb;
import lombok.extern.log4j.Log4j;
import com.google.inject.Singleton;
import javax.inject.Inject;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.JsonSuccess;
import com.distelli.europa.Constants;
import com.distelli.persistence.PageIterator;

@Log4j
@Singleton
public class SetAuthTokenStatus extends AjaxHelper
{
    @Inject
    private TokenAuthDb _tokenAuthDb;

    public SetAuthTokenStatus()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest)
    {
        TokenAuth tokenAuth = ajaxRequest.convertContent(TokenAuth.class, true);
        FieldValidator.validateNonNull(tokenAuth, "token", "status");

        _tokenAuthDb.setStatus(tokenAuth.getToken(),
                               tokenAuth.getStatus());
        return JsonSuccess.Success;
    }
}
