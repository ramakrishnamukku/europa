/*
  $Id: $
  @file ListRepoManifests.java
  @brief Contains the ListRepoManifests.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.ajax;

import com.distelli.persistence.PageIterator;

import com.distelli.europa.util.PermissionCheck;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.webserver.*;
import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;
import com.distelli.europa.EuropaRequestContext;

@Log4j
@Singleton
public class ListRepoManifests extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private RegistryManifestDb _db;
    @Inject
    protected ContainerRepoDb _repoDb;
    @Inject
    protected PermissionCheck _permissionCheck;

    public ListRepoManifests()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String repoId = ajaxRequest.getParam("repoId", true);
        int pageSize = ajaxRequest.getParamAsInt("pageSize", 100);
        String marker = ajaxRequest.getParam("marker");
        String domain = requestContext.getOwnerDomain();

        ContainerRepo repo = _repoDb.getRepo(domain, repoId);
        if(repo == null)
            throw(new AjaxClientException("The specified Repository was not found",
                                          AjaxErrors.Codes.RepoNotFound, 400));
        _permissionCheck.check(ajaxRequest, requestContext, repo);

        PageIterator pageIterator = new PageIterator()
        .pageSize(pageSize)
        .marker(marker)
        .forward();
        return _db.listManifestsByRepoId(domain, repoId, pageIterator);
    }
}
