/*
  $Id: $
  @file SaveContainerRepo.java
  @brief Contains the SaveContainerRepo.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import org.apache.log4j.Logger;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.webserver.*;
import com.distelli.europa.util.*;

import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SaveContainerRepo implements AjaxHelper
{
    private static final Logger log = Logger.getLogger(SaveContainerRepo.class);

    @Inject
    private ContainerRepoDb _db;
    @Inject
    private NotificationsDb _notificationDb;

    public SaveContainerRepo()
    {

    }

    public Object get(AjaxRequest ajaxRequest)
    {
        ContainerRepo repo = ajaxRequest.convertContent("/repo", ContainerRepo.class,
                                                       true); //throw if null
        //Validate that the fields we want are non-null
        FieldValidator.validateNonNull(repo, "provider", "region", "credName", "name");

        Notification notification = ajaxRequest.convertContent("/notification", Notification.class,
                                                               true);
        FieldValidator.validateNonNull(notification, "type", "target");
        //save the repo in the db
        _db.save(repo);
        notification.setRepoProvider(repo.getProvider());
        notification.setRegion(repo.getRegion());
        notification.setRepoName(repo.getName());
        _notificationDb.save(notification);
        return JsonSuccess.Success;
    }
}
