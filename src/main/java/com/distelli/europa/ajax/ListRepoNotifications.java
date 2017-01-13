/*
  $Id: $
  @file ListRepoNotifications.java
  @brief Contains the ListRepoNotifications.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.ajax;

import com.distelli.persistence.PageIterator;

import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.webserver.*;
import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class ListRepoNotifications extends AjaxHelper
{
    @Inject
    private NotificationsDb _db;

    public ListRepoNotifications()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest, RequestContext requestContext)
    {
        String repoId = ajaxRequest.getParam("repoId", true);
        int pageSize = ajaxRequest.getParamAsInt("pageSize", 100);
        String marker = ajaxRequest.getParam("marker");
        String domain = ajaxRequest.getParam("domain");

        PageIterator pageIterator = new PageIterator()
        .pageSize(pageSize)
        .marker(marker)
        .forward();

        return _db.listNotifications(domain, repoId, pageIterator);
    }
}
