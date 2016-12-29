/*
  $Id: $
  @file DeleteRegistryCreds.java
  @brief Contains the DeleteRegistryCreds.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import com.distelli.europa.models.*;
import com.distelli.europa.db.*;
import com.distelli.ventura.*;
import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;
import org.eclipse.jetty.http.HttpMethod;

@Log4j
@Singleton
public class DeleteRegistryCreds extends AjaxHelper
{
    @Inject
    private RegistryCredsDb _db;

    public DeleteRegistryCreds()
    {
        this.supportedHttpMethods.add(HttpMethod.POST);
    }

    /**
       Params:
       - Provider (reqired)
       - Region (required)
    */
    public Object get(AjaxRequest ajaxRequest)
    {
        String id = ajaxRequest.getParam("id",
                                         true); //throw if missing
        String domain = ajaxRequest.getParam("domain");
        _db.deleteCred(domain, id);
        return JsonSuccess.Success;
    }
}
