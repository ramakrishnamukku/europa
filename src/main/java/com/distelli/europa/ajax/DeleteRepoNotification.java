/*
  $Id: $
  @file DeleteRepoNotification.java
  @brief Contains the DeleteRepoNotification.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

import com.distelli.europa.clients.*;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.monitor.*;
import com.distelli.europa.util.*;
import com.distelli.ventura.*;
import com.distelli.gcr.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.models.*;
import com.distelli.persistence.PageIterator;
import com.google.inject.Singleton;
import org.eclipse.jetty.http.HttpMethod;
import lombok.extern.log4j.Log4j;
import javax.inject.Inject;

@Log4j
@Singleton
public class DeleteRepoNotification extends AjaxHelper
{
    @Inject
    private NotificationsDb _notificationDb;
    @Inject
    private MonitorQueue _monitorQueue;

    public DeleteRepoNotification()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest)
    {
        String notificationId = ajaxRequest.getParam("notificationId", true);
        String repoId = ajaxRequest.getParam("repoId", true);
        String domain = ajaxRequest.getParam("domain");
        NotificationType notificationType = ajaxRequest.getParamAsEnum("type", NotificationType.class, true);
        _notificationDb.deleteNotification(domain, repoId, notificationType, notificationId);
        _monitorQueue.setReload(true);
        return JsonSuccess.Success;
    }
}
