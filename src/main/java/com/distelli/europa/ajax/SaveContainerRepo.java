/*
  $Id: $
  @file SaveContainerRepo.java
  @brief Contains the SaveContainerRepo.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import javax.inject.Inject;

import java.net.MalformedURLException;
import java.net.URL;

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

@Log4j
@Singleton
public class SaveContainerRepo extends AjaxHelper
{
    @Inject
    private RegistryCredsDb _credsDb;
    @Inject
    private ContainerRepoDb _reposDb;
    @Inject
    private NotificationsDb _notificationDb;
    @Inject
    private MonitorQueue _monitorQueue;

    public SaveContainerRepo()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
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
        validateContainerRepo(repo, cred);
        //before we save the repo in the db lets check that the repo
        //doesn't already exist
        if(_reposDb.repoExists(repo.getDomain(), repo.getProvider(), repo.getRegion(), repo.getName()))
            throw(new AjaxClientException("The specified container repo is already connected: "+
                                          repo.getProvider()+", "+repo.getName(),
                                          AjaxErrors.Codes.RepoAlreadyConnected,
                                          400));
        Notification notification = ajaxRequest.convertContent("/notification", Notification.class, false);
        if(notification != null) {
            FieldValidator.validateNonNull(notification, "type", "target");

            try {
                String urlStr = notification.getTarget();
                if(!urlStr.startsWith("http://") && !urlStr.startsWith("https://"))
                    urlStr = "http://"+urlStr;
                URL url = new URL(urlStr);
            } catch(MalformedURLException mue) {
                throw(new AjaxClientException("Invalid Target URL on Webhook Notification: "+notification.getTarget(),
                                              JsonError.Codes.BadContent, 400));
            }

            notification.setRepoId(repo.getId());
            notification.setDomain(repo.getDomain());
            notification.setRepoProvider(repo.getProvider());
            notification.setRegion(repo.getRegion());
            notification.setRepoName(repo.getName());
        }
        //save the repo in the db
        _reposDb.save(repo);

        if(notification != null)
            _notificationDb.save(notification);

        _monitorQueue.setReload(true);
        HashMap<String, String> retVal = new HashMap<String, String>();
        retVal.put("id", repo.getId());
        return retVal;
    }

    private void validateContainerRepo(ContainerRepo repo, RegistryCred cred)
    {
        RegistryProvider provider = cred.getProvider();
        switch(provider)
        {
        case ECR:
            validateEcrRepo(repo, cred);
            break;
        case GCR:
            validateGcrRepo(repo, cred);
            break;
        default:
            throw(new AjaxClientException("Unsupported Container Registry: "+provider, JsonError.Codes.BadContent, 400));
        }
    }

    private void validateGcrRepo(ContainerRepo repo, RegistryCred cred)
    {
        GcrCredentials gcrCreds = new GcrServiceAccountCredentials(cred.getSecret());
        GcrRegion gcrRegion = GcrRegion.getRegion(cred.getRegion());
        GcrClient gcrClient = new GcrClient(gcrCreds, gcrRegion);

        GcrIterator iter = GcrIterator.builder().pageSize(1).build();
        try {
            List<GcrImageTag> tags = gcrClient.listImageTags(repo.getName(), iter);
        } catch(Throwable t) {
            throw(new AjaxClientException("Invalid Container Repository or Credentials: "+t.getMessage(), JsonError.Codes.BadContent, 400));
        }
   }

    private void validateEcrRepo(ContainerRepo repo, RegistryCred cred)
    {
        ECRClient ecrClient = new ECRClient(cred);
        PageIterator iter = new PageIterator().pageSize(1);
        try {
            List<DockerImageId> images = ecrClient.listImages(repo, iter);
        } catch(Throwable t) {
            throw(new AjaxClientException("Invalid Container Repository or Credentials: "+t.getMessage(), JsonError.Codes.BadContent, 400));
        }
    }
}
