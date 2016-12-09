/*
  $Id: $
  @file GetRegistryCreds.java
  @brief Contains the GetRegistryCreds.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import org.apache.log4j.Logger;
import com.distelli.europa.models.*;
import com.distelli.europa.db.*;
import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GetRegistryCreds implements AjaxHelper
{
    private static final Logger log = Logger.getLogger(GetRegistryCreds.class);

    @Inject
    private RegistryCredsDb _db;

    public GetRegistryCreds()
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
        RegistryCred cred = _db.getCred(id);
        if(cred != null)
            cred.setSecret(null);
        return cred;
    }
}
