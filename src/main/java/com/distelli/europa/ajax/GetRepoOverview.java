/*
  $Id: $
  @file GetRepoOverview.java
  @brief Contains the GetRepoOverview.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.ContainerRepoDb;
import com.distelli.europa.models.ContainerRepo;
import com.distelli.europa.models.RepoOverviewContent;
import com.distelli.europa.util.ObjectKeyFactory;
import com.distelli.objectStore.ObjectKey;
import com.distelli.objectStore.ObjectStore;
import com.distelli.webserver.AjaxClientException;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class GetRepoOverview extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private ContainerRepoDb _repoDb;
    @Inject
    protected Provider<ObjectStore> _objectStoreProvider;
    @Inject
    protected Provider<ObjectKeyFactory> _objectKeyFactoryProvider;

    public GetRepoOverview()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String ownerDomain = requestContext.getOwnerDomain();
        String repoId = ajaxRequest.getParam("repoId", true);
        ContainerRepo repo = _repoDb.getRepo(ownerDomain, repoId);
        if(repo == null)
            throw(new AjaxClientException("The specified Repository was not found",
                                          AjaxErrors.Codes.RepoNotFound, 400));
        String overviewId = repo.getOverviewId();
        ObjectKeyFactory objectKeyFactory = _objectKeyFactoryProvider.get();
        ObjectKey objectKey = objectKeyFactory.forRepoOverview(overviewId);
        String contentData = null;
        try {
            ObjectStore objectStore = _objectStoreProvider.get();
            byte[] overviewBytes = objectStore.get(objectKey);
            contentData = new String(overviewBytes, "UTF-8");
        } catch(EntityNotFoundException enfe) {
            //if the object wasn't found then content data is null
            contentData = null;
        } catch(IOException ioe) {
            throw(new RuntimeException(ioe));
        }
        RepoOverviewContent content = RepoOverviewContent
        .builder()
        .content(contentData)
        .build();
        return content;
    }
}
