package com.distelli.europa.ajax;

import com.distelli.europa.db.TokenAuthDb;
import lombok.extern.log4j.Log4j;
import com.google.inject.Singleton;
import javax.inject.Inject;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.europa.Constants;
import com.distelli.persistence.PageIterator;

@Log4j
@Singleton
public class ListAuthTokens extends AjaxHelper
{
    @Inject
    private TokenAuthDb _tokenAuthDb;

    public ListAuthTokens()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest)
    {
        return _tokenAuthDb.getTokens(Constants.DOMAIN_ZERO, new PageIterator().pageSize(1000));
    }
}
