/*
  $Id: $
  @file DeleteRegistryCreds.java
  @brief Contains the DeleteRegistryCreds.java class

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
public class DeleteRegistryCreds implements AjaxHelper
{
    private static final Logger log = Logger.getLogger(DeleteRegistryCreds.class);

    @Inject
    private RegistryCredsDb _db;

    public DeleteRegistryCreds()
    {

    }

    /**
       Params:
       - Provider (reqired)
       - Region (required)
    */
    public Object get(AjaxRequest ajaxRequest)
    {
        RegistryProvider provider = ajaxRequest.getAsEnum("provider",
                                                          RegistryProvider.class,
                                                          true); //throw if missing
        String region = ajaxRequest.getParam("region", true);
        // String name = ajaxRequest.getParam("name", true);
        _db.deleteCred(provider, region);
        return JsonSuccess.Success;
    }
}
