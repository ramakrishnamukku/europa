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
       - Provider (reqired)
       - Region (required)
       - Name (required)
    */
    public Object get(AjaxRequest ajaxRequest)
    {
        RegistryProvider provider = ajaxRequest.getAsEnum("provider",
                                                          RegistryProvider.class,
                                                          true); //throw if missing
        String region = ajaxRequest.getParam("region", true);
        String name = ajaxRequest.getParam("name", true);
        _db.deleteRepo(provider, region, name);
        return JsonSuccess.Success;
    }
}
