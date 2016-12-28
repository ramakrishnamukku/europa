/*
  $Id: $
  @file ListRepoEvents.java
  @brief Contains the ListRepoEvents.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import com.distelli.persistence.PageIterator;

import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.ventura.*;
import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class ListRepoEvents implements AjaxHelper
{
    @Inject
    private RepoEventsDb _db;

    public ListRepoEvents()
    {

    }

    public Object get(AjaxRequest ajaxRequest)
    {
        String repoId = ajaxRequest.getParam("repoId", true);
        int pageSize = ajaxRequest.getParamAsInt("pageSize", 100);
        String marker = ajaxRequest.getParam("marker");
        String domain = ajaxRequest.getParam("domain");

        PageIterator pageIterator = new PageIterator()
        .pageSize(pageSize)
        .marker(marker)
        .forward();

        return _db.listEvents(domain, repoId, pageIterator);
    }
}
