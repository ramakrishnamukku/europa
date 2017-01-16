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
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;
import org.eclipse.jetty.http.HttpMethod;
import com.distelli.europa.EuropaRequestContext;

@Log4j
@Singleton
public class GetContainerRepo extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private ContainerRepoDb _db;

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
        return _db.getRepo(domain, id);
    }
}
