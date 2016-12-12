/*
  $Id: $
  @file DeleteContainerRepo.java
  @brief Contains the DeleteContainerRepo.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import org.apache.log4j.Logger;
import com.distelli.europa.models.*;
import com.distelli.europa.db.*;
import com.distelli.europa.webserver.*;
import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DeleteContainerRepo implements AjaxHelper
{
    private static final Logger log = Logger.getLogger(DeleteContainerRepo.class);

    @Inject
    private ContainerRepoDb _db;

    public DeleteContainerRepo()
    {

    }

    /**
       Params:
       - id (reqired)
    */
    public Object get(AjaxRequest ajaxRequest)
    {
        String id = ajaxRequest.getParam("id",
                                         true); //throw if missing
        String domain = ajaxRequest.getParam("domain");
        _db.deleteRepo(domain, id);
        return JsonSuccess.Success;
    }
}
