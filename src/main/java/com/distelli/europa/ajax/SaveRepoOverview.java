/*
  $Id: $
  @file SaveRepoOverview.java
  @brief Contains the SaveRepoOverview.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.ContainerRepoDb;
import com.distelli.europa.models.ContainerRepo;
import com.distelli.europa.models.RepoOverviewContent;
import com.distelli.europa.util.FieldValidator;
import com.distelli.europa.util.ObjectKeyFactory;
import com.distelli.objectStore.ObjectKey;
import com.distelli.objectStore.ObjectStore;
import com.distelli.webserver.AjaxClientException;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.JsonSuccess;
import com.distelli.europa.util.PermissionCheck;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class SaveRepoOverview extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private ContainerRepoDb _repoDb;
    @Inject
    protected Provider<ObjectStore> _objectStoreProvider;
    @Inject
    protected Provider<ObjectKeyFactory> _objectKeyFactoryProvider;
    @Inject
    protected PermissionCheck _permissionCheck;

    public SaveRepoOverview()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String ownerDomain = requestContext.getOwnerDomain();
        String repoId = ajaxRequest.getParam("repoId", true);
        RepoOverviewContent overviewContent = ajaxRequest.convertContent(RepoOverviewContent.class,
                                                                 true);
        FieldValidator.validateNonNull(overviewContent, "content");
        ContainerRepo repo = _repoDb.getRepo(ownerDomain, repoId);
        if(repo == null)
            throw(new AjaxClientException("The specified Repository was not found",
                                          AjaxErrors.Codes.RepoNotFound, 400));

        _permissionCheck.check(ajaxRequest.getOperation(), requestContext, repo);

        String overviewId = repo.getOverviewId();
        ObjectKeyFactory objectKeyFactory = _objectKeyFactoryProvider.get();
        ObjectKey objectKey = objectKeyFactory.forRepoOverview(overviewId);

        ObjectStore objectStore = _objectStoreProvider.get();
        objectStore.put(objectKey, overviewContent.getContent().getBytes());

        return JsonSuccess.Success;
    }
}
