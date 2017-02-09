/*
  $Id: $
  @file ListRepoManifests.java
  @brief Contains the ListRepoManifests.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.ajax;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

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

        _permissionCheck.check(ajaxRequest, requestContext, repoId);

        int dbReadPageSize = pageSize*3;

        PageIterator pageIterator = new PageIterator()
        .pageSize(dbReadPageSize)
        .marker(marker)
        .forward();

        List<RegistryManifest> manifestList = _db.listManifestsByRepoManifestId(domain, repoId, pageIterator);
        if(manifestList == null || manifestList.size() == 0)
            return manifestList;

        List<MultiTaggedManifest> taggedManifests = new ArrayList<MultiTaggedManifest>();
        MultiTaggedManifest curItem = null;
        for(RegistryManifest manifest : manifestList)
        {
            if(curItem == null) {
                if(taggedManifests.size() >= pageSize)
                    break;
                curItem = MultiTaggedManifest.fromRegistryManifest(manifest);
            }
            else
            {
                if(curItem.getManifestId().equalsIgnoreCase(manifest.getManifestId()))
                    curItem.addTag(manifest.getTag());
                else
                {
                    taggedManifests.add(curItem);
                    curItem = MultiTaggedManifest.fromRegistryManifest(manifest);
                }
            }
        }

        return taggedManifests;
    }
}
