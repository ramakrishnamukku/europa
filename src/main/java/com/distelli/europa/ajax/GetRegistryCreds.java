/*
  $Id: $
  @file GetRegistryCreds.java
  @brief Contains the GetRegistryCreds.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import javax.inject.Inject;

import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import com.distelli.webserver.RequestContext;
import com.google.inject.Singleton;

import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class GetRegistryCreds extends AjaxHelper
{
    @Inject
    private RegistryCredsDb _db;

    public GetRegistryCreds()
    {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    /**
       Params:
       - id (reqired)
    */
    public Object get(AjaxRequest ajaxRequest, RequestContext requestContext)
    {
        String id = ajaxRequest.getParam("id",
                                         true); //throw if missing
        String domain = ajaxRequest.getParam("domain");
        RegistryCred cred = _db.getCred(domain, id);
        if(cred != null)
            cred.setSecret(null);
        return cred;
    }
}
