/*
  $Id: $
  @file ListContainerRepos.java
  @brief Contains the ListContainerRepos.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jetty.http.HttpMethod;
import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.util.PermissionCheck;
import com.distelli.persistence.PageIterator;
import com.distelli.webserver.*;
import com.google.inject.Singleton;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class ListContainerRepos extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private ContainerRepoDb _db;
    @Inject
    protected PermissionCheck _permissionCheck;

    public ListContainerRepos()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    /**
       Params:
       - Provider (optional)
       - Region (optional)
       - pageSize (optional)
       - marker (optional)
    */
    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        RegistryProvider provider = ajaxRequest.getParamAsEnum("provider", RegistryProvider.class);
        String region = ajaxRequest.getParam("region");
        int pageSize = ajaxRequest.getParamAsInt("pageSize", 100);
        String marker = ajaxRequest.getParam("marker");
        String domain = requestContext.getOwnerDomain();

        PageIterator pageIterator = new PageIterator()
        .pageSize(pageSize)
        .marker(marker)
        .forward();

        List<ContainerRepo> repoList = null;
        if(provider == null && region == null)
            repoList = _db.listRepos(domain, pageIterator);
        else if(provider != null)
        {
            if(region != null)
                repoList = _db.listRepos(domain, provider, region, pageIterator);
            else
                repoList = _db.listRepos(domain, provider, pageIterator);
        }
        else
            repoList = _db.listRepos(domain, pageIterator);

        if(repoList == null || repoList.size() == 0)
            return repoList;
        Map<ContainerRepo, Boolean> permissionResult = _permissionCheck.checkBatch(ajaxRequest.getOperation(),
                                                                                   requestContext,
                                                                                   repoList);
        List<ContainerRepo> retval = new ArrayList<ContainerRepo>();
        for(ContainerRepo repo : repoList)
        {
            Boolean allow = permissionResult.get(repo);
            if(allow == null || allow == Boolean.FALSE)
                continue;
            retval.add(repo);
        }

        return retval;
    }
}
