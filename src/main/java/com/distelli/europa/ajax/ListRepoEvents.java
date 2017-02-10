/*
  $Id: $
  @file ListRepoEvents.java
  @brief Contains the ListRepoEvents.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.ajax;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.inject.Inject;
import java.util.Collections;
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
        int pageSize = ajaxRequest.getParamAsInt("pageSize", 20);
        String marker = ajaxRequest.getParam("marker");
        String domain = requestContext.getOwnerDomain();

        _permissionCheck.check(ajaxRequest.getOperation(), requestContext, repoId);

        boolean dbAscending = Boolean.parseBoolean(ajaxRequest.getParam("backward"));

        PageIterator pageIterator = new PageIterator()
        .pageSize(pageSize)
        .marker(marker);
        if(!dbAscending)
            pageIterator.backward();

        List<RepoEvent> events = _db.listEvents(domain, repoId, pageIterator);

        if(dbAscending) {
            List<RepoEvent> reversedEvents = new ArrayList<RepoEvent>();
            reversedEvents.addAll(events);
            Collections.reverse(reversedEvents);
            events = reversedEvents;
        }

        RepoEventList eventList = RepoEventList
        .builder()
        .prevMarker(dbAscending ? pageIterator.getMarker() : pageIterator.getPrevMarker())
        .nextMarker(dbAscending ? pageIterator.getPrevMarker() : pageIterator.getMarker())
        .events(events)
        .build();
        return eventList;
    }
}
