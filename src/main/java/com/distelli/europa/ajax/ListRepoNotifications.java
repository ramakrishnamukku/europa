/*
  $Id: $
  @file ListRepoNotifications.java
  @brief Contains the ListRepoNotifications.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.ajax;

import com.distelli.persistence.PageIterator;

import com.distelli.europa.util.PermissionCheck;
import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.webserver.*;
import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class ListRepoNotifications extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private NotificationsDb _db;
    @Inject
    protected PermissionCheck _permissionCheck;

    public ListRepoNotifications()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String repoId = ajaxRequest.getParam("repoId", true);
        int pageSize = ajaxRequest.getParamAsInt("pageSize", 100);
        String marker = ajaxRequest.getParam("marker");
        String domain = requestContext.getOwnerDomain();

        _permissionCheck.check(ajaxRequest.getOperation(), requestContext, repoId);

        PageIterator pageIterator = new PageIterator()
        .pageSize(pageSize)
        .marker(marker)
        .forward();

        return _db.listNotifications(domain, repoId, pageIterator);
    }
}
