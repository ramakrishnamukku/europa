/*
  $Id: $
  @file SaveRegistryCreds.java
  @brief Contains the SaveRegistryCreds.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.ajax;

import org.apache.log4j.Logger;
import com.distelli.europa.db.*;
import com.distelli.europa.models.*;
import com.distelli.europa.webserver.*;
import com.distelli.europa.util.*;

import javax.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SaveRegistryCreds implements AjaxHelper
{
    private static final Logger log = Logger.getLogger(SaveRegistryCreds.class);

    @Inject
    private RegistryCredsDb _db;

    public SaveRegistryCreds()
    {

    }

    public Object get(AjaxRequest ajaxRequest)
    {
        RegistryCred cred = ajaxRequest.convertContent(RegistryCred.class,
                                                       true); //throw if null
        //Validate that the fields we want are non-null
        FieldValidator.validateNonNull(cred, "provider", "region", "key", "secret");
        cred.setCreated(System.currentTimeMillis());
        //save in the db
        _db.save(cred);
        return JsonSuccess.Success;
    }
}
