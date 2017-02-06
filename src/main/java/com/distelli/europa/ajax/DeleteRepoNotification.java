/*
  $Id: $
  @file DeleteRepoNotification.java
  @brief Contains the DeleteRepoNotification.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.List;
import javax.inject.Inject;

import com.distelli.europa.clients.*;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.util.*;
import com.distelli.webserver.*;
import com.distelli.gcr.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.models.*;
import com.distelli.persistence.PageIterator;
import com.google.inject.Singleton;
import org.eclipse.jetty.http.HttpMethod;
import lombok.extern.log4j.Log4j;
import javax.inject.Inject;
import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.util.PermissionCheck;

@Log4j
@Singleton
public class DeleteRepoNotification extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private NotificationsDb _notificationDb;
    @Inject
    protected PermissionCheck _permissionCheck;

    public DeleteRepoNotification()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String notificationId = ajaxRequest.getParam("notificationId", true);
        String repoId = ajaxRequest.getParam("repoId", true);
        String domain = requestContext.getOwnerDomain();
        _permissionCheck.check(ajaxRequest, requestContext, repoId);

        _notificationDb.deleteNotification(domain, notificationId);
        return JsonSuccess.Success;
    }
}
