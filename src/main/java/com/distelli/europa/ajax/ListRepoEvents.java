/*
  $Id: $
  @file ListRepoEvents.java
  @brief Contains the ListRepoEvents.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.ajax;

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
public class ListRepoEvents extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    protected RepoEventsDb _db;
    @Inject
    protected PermissionCheck _permissionCheck;

    public ListRepoEvents()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext)
    {
        String repoId = ajaxRequest.getParam("repoId", true);
        int pageSize = ajaxRequest.getParamAsInt("pageSize", 100);
        String marker = ajaxRequest.getParam("marker");
        String domain = requestContext.getOwnerDomain();

        _permissionCheck.check(ajaxRequest.getOperation(), requestContext, repoId);

        boolean isForward = Boolean.parseBoolean(ajaxRequest.getParam("forward"));

        PageIterator pageIterator = new PageIterator()
        .pageSize(pageSize)
        .marker(marker);
        if(!isForward)
            pageIterator.backward();

        List<RepoEvent> events = _db.listEvents(domain, repoId, pageIterator);
        RepoEventList eventList = RepoEventList
        .builder()
        .nextMarker(isForward ? pageIterator.getMarker() : pageIterator.getPrevMarker())
        .prevMarker(isForward ? pageIterator.getPrevMarker() : pageIterator.getMarker())
        .events(events)
        .build();
        return eventList;
    }
}
