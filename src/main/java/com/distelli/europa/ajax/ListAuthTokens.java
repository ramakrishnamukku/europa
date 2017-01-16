package com.distelli.europa.ajax;

import javax.inject.Inject;

import com.distelli.europa.Constants;
import com.distelli.europa.db.TokenAuthDb;
import com.distelli.persistence.PageIterator;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.RequestContext;
import com.google.inject.Singleton;
import com.distelli.europa.EuropaRequestContext;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class ListAuthTokens extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private TokenAuthDb _tokenAuthDb;

    public ListAuthTokens()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        return _tokenAuthDb.getTokens(requestContext.getOwnerDomain(),
                                      new PageIterator().pageSize(1000));
    }
}
