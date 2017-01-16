/*
  $Id: $
  @file SaveRepoNotification.java
  @brief Contains the SaveRepoNotification.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
import java.net.MalformedURLException;
import java.net.URL;
import com.distelli.europa.EuropaRequestContext;

@Log4j
@Singleton
public class SaveRepoNotification extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private ContainerRepoDb _repoDb;
    @Inject
    private NotificationsDb _notificationDb;
    @Inject
    private MonitorQueue _monitorQueue;

    public SaveRepoNotification()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String repoId = ajaxRequest.getParam("repoId", true);
        String repoDomain = requestContext.getOwnerDomain();
        ContainerRepo repo = _repoDb.getRepo(repoDomain, repoId);
        if(repo == null)
            throw(new AjaxClientException("Invalid RepoId: "+repoId, JsonError.Codes.BadParam, 400));
        Notification notification = ajaxRequest.convertContent("/notification", Notification.class,
                                                               true);
        FieldValidator.validateNonNull(notification, "type", "target");
        //now check that the target is a valid URL
        try {
            URL url = new URL(notification.getTarget());
        } catch(MalformedURLException mue) {
            throw(new AjaxClientException("Invalid Target URL on Webhook Notification: "+notification.getTarget(),
                                          JsonError.Codes.BadContent, 400));
        }
        notification.setRepoId(repo.getId());
        notification.setDomain(repoDomain);
        notification.setRepoProvider(repo.getProvider());
        notification.setRegion(repo.getRegion());
        notification.setRepoName(repo.getName());
        _notificationDb.save(notification);
        _monitorQueue.setReload(true);
        HashMap<String, String> retVal = new HashMap<String, String>();
        retVal.put("id", notification.getId());
        return retVal;
    }
}
