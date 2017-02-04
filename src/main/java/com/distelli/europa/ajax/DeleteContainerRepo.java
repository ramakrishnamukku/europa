/*
  $Id: $
  @file DeleteContainerRepo.java
  @brief Contains the DeleteContainerRepo.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import com.distelli.europa.models.*;
import com.distelli.europa.db.*;
import com.distelli.webserver.*;
import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;
import org.eclipse.jetty.http.HttpMethod;
import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.util.PermissionCheck;

@Log4j
@Singleton
public class DeleteContainerRepo extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private ContainerRepoDb _repoDb;
    @Inject
    protected PermissionCheck _permissionCheck;

    public DeleteContainerRepo()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    /**
       Params:
       - id (reqired)
    */
    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String repoId = ajaxRequest.getParam("id",
                                         true); //throw if missing
        String domain = requestContext.getOwnerDomain();
        ContainerRepo repo = _repoDb.getRepo(domain, repoId);
        if(repo == null)
            throw(new AjaxClientException("The specified Repository was not found",
                                          AjaxErrors.Codes.RepoNotFound, 400));
        _permissionCheck.check(ajaxRequest, requestContext, repo);
        _repoDb.deleteRepo(domain, repoId);
        return JsonSuccess.Success;
    }
}
