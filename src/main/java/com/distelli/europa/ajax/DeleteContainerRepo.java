/*
  $Id: $
  @file DeleteContainerRepo.java
  @brief Contains the DeleteContainerRepo.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import com.distelli.europa.models.*;
import com.distelli.europa.db.*;
import com.distelli.webserver.*;
import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;
import org.eclipse.jetty.http.HttpMethod;
import com.distelli.europa.EuropaRequestContext;

@Log4j
@Singleton
public class DeleteContainerRepo extends AjaxHelper<EuropaRequestContext>
{
    @Inject
    private ContainerRepoDb _db;

    public DeleteContainerRepo()
    {
        this.supportedHttpMethods.add(HTTPMethod.POST);
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
        _db.deleteRepo(domain, id);
        return JsonSuccess.Success;
    }
}
