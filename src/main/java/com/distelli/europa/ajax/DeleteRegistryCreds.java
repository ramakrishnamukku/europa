/*
  $Id: $
  @file DeleteRegistryCreds.java
  @brief Contains the DeleteRegistryCreds.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.List;
import javax.inject.Inject;

import com.distelli.europa.util.PermissionCheck;
import org.eclipse.jetty.http.HttpMethod;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.persistence.PageIterator;
import com.distelli.webserver.*;
import com.google.inject.Singleton;
import com.distelli.europa.EuropaRequestContext;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class DeleteRegistryCreds extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private RegistryCredsDb _db;
    @Inject
    private ContainerRepoDb _reposDb;
    @Inject
    protected PermissionCheck _permissionCheck;

    public DeleteRegistryCreds()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    /**
       Params:
       - Provider (reqired)
       - Region (required)
    */
    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        _permissionCheck.check(ajaxRequest.getOperation(), requestContext);
        String id = ajaxRequest.getParam("id",
                                         true); //throw if missing
        String domain = requestContext.getOwnerDomain();
        //Before we delete the cred lets check to ensure that there is
        //no container repo thats using these creds
        List<ContainerRepo> repos = _reposDb.listReposByCred(domain, id, new PageIterator().pageSize(10));
        if(repos != null && repos.size() > 0)
            throw(new AjaxClientException("The specified creds are in use by connected repositories. "+
                                          "Please disconnect the repositories and try again",
                                          AjaxErrors.Codes.RepoAlreadyConnected,
                                          400));
        _db.deleteCred(domain, id);
        return JsonSuccess.Success;
    }
}
