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
import com.distelli.europa.monitor.*;
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

@Log4j
@Singleton
public class DeleteRepoNotification extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private NotificationsDb _notificationDb;
    @Inject
    private MonitorQueue _monitorQueue;

    public DeleteRepoNotification()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String notificationId = ajaxRequest.getParam("notificationId", true);
        String domain = requestContext.getOwnerDomain();
        _notificationDb.deleteNotification(domain, notificationId);
        _monitorQueue.setReload(true);
        return JsonSuccess.Success;
    }
}
