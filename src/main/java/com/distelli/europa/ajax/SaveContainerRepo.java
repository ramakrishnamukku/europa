/*
  $Id: $
  @file SaveContainerRepo.java
  @brief Contains the SaveContainerRepo.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.UUID;
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
    private RegistryCredsDb _credsDb;
    @Inject
    private ContainerRepoDb _reposDb;
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
        FieldValidator.validateNonNull(repo, "credId", "name");
        //Now get the cred from the credId
        RegistryCred cred = _credsDb.getCred(repo.getDomain(), repo.getCredId());
        if(cred == null)
            throw(new AjaxClientException("Invalid Registry Cred: "+repo.getCredId(), JsonError.Codes.BadContent, 400));
        repo.setProvider(cred.getProvider());
        repo.setRegion(cred.getRegion());
        repo.setId(UUID.randomUUID().toString());

        Notification notification = ajaxRequest.convertContent("/notification", Notification.class,
                                                               true);
        FieldValidator.validateNonNull(notification, "type", "target");
        //save the repo in the db
        _reposDb.save(repo);
        notification.setRepoId(repo.getId());
        notification.setDomain(repo.getDomain());
        notification.setRepoProvider(repo.getProvider());
        notification.setRegion(repo.getRegion());
        notification.setRepoName(repo.getName());
        _notificationDb.save(notification);
        return JsonSuccess.Success;
    }
}
