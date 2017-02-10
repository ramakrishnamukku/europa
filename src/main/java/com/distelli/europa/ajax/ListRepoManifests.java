/*
  $Id: $
  @file ListRepoManifests.java
  @brief Contains the ListRepoManifests.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.ajax;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
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
    private RegistryManifestDb _manifestDb;
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

        PageIterator iter = new PageIterator()
            .pageSize(ajaxRequest.getParamAsInt("pageSize", 100))
            .marker(ajaxRequest.getParam("marker"))
            .setIsForward(!Boolean.parseBoolean(ajaxRequest.getParam("backward")));

        List<MultiTaggedManifest> list = _manifestDb.listMultiTaggedManifest(
            requestContext.getOwnerDomain(),
            ajaxRequest.getParam("repoId", true),
            iter);
        if ( ! iter.isForward() ) Collections.reverse(list);
        return Page.<MultiTaggedManifest>builder()
            .list(list)
            .next(iter.isForward() ? iter.getMarker() : iter.getPrevMarker())
            .prev(iter.isForward() ? iter.getPrevMarker() : iter.getMarker())
            .build();
    }
}
