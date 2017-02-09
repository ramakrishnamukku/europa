/*
  $Id: $
  @file SetRepoPublic.java
  @brief Contains the SetRepoPublic.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.ContainerRepoDb;
import com.distelli.europa.models.ContainerRepo;
import com.distelli.europa.models.RegistryProvider;
import com.distelli.europa.util.PermissionCheck;
import com.distelli.webserver.AjaxClientException;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.JsonError;
import com.distelli.webserver.JsonSuccess;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class SetRepoPublic extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private ContainerRepoDb _repoDb;
    @Inject
    protected PermissionCheck _permissionCheck;

    public SetRepoPublic()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String ownerDomain = requestContext.getOwnerDomain();
        String repoId = ajaxRequest.getParam("repoId", true);
        ContainerRepo repo = _repoDb.getRepo(ownerDomain, repoId);
        if(repo == null)
            throw(new AjaxClientException("The specified Repository was not found",
                                          AjaxErrors.Codes.RepoNotFound, 400));
        _permissionCheck.check(ajaxRequest.getOperation(), requestContext, repo);

        if(!repo.isLocal())
            throw(new AjaxClientException("Cannot change public / private setting for Remote Repository",
                                          JsonError.Codes.UnsupportedOperation, 400));

        String isPublic = ajaxRequest.getParam("public", true);
        if(isPublic != null && isPublic.equalsIgnoreCase("true"))
            _repoDb.setRepoPublic(ownerDomain, repoId);
        else
            _repoDb.setRepoPrivate(ownerDomain, repoId);
        return JsonSuccess.Success;
    }
}
