/*
  $Id: $
  @file GetContainerRepo.java
  @brief Contains the GetContainerRepo.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.webserver.*;
import com.distelli.europa.util.*;

import javax.inject.Inject;
import javax.inject.Provider;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;
import org.eclipse.jetty.http.HttpMethod;
import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.util.PermissionCheck;

@Log4j
@Singleton
public class GetContainerRepo extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private ContainerRepoDb _db;
    @Inject
    protected PermissionCheck _permissionCheck;
    @Inject
    private Provider<DnsSettings> _dnsSettingsProvider;

    public GetContainerRepo()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    /**
       Params:
       - id (reqired)
    */
    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String id = ajaxRequest.getParam("id",
                                         true); //throw if missing
        String domain = requestContext.getOwnerDomain();
        ContainerRepo repo = _db.getRepo(domain, id);
        if(repo == null)
            throw(new AjaxClientException("The specified Repository was not found",
                                          AjaxErrors.Codes.RepoNotFound, 400));
        _permissionCheck.check(ajaxRequest.getOperation(), requestContext, repo);

        if(repo.isLocal())
        {
            DnsSettings dnsSettings = _dnsSettingsProvider.get();
            if(dnsSettings == null)
                dnsSettings = DnsSettings.fromHostHeader(requestContext);
            repo.setEndpoint(dnsSettings.getDnsName());
        }

        return repo;
    }
}
